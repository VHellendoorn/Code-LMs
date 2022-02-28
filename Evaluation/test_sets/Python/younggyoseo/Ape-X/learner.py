"""
Module for learner in Ape-X.
"""
import time
import os
import _pickle as pickle
import threading
import queue

import torch
import torch.multiprocessing as mp
from torch.multiprocessing import Process, Queue
from tensorboardX import SummaryWriter
import numpy as np
import zmq

import utils
import wrapper
from model import DuelingDQN
from arguments import argparser


def get_environ():
    n_actors = int(os.environ.get('N_ACTORS', '-1'))
    replay_ip = os.environ.get('REPLAY_IP', '-1')
    assert (n_actors != -1 and replay_ip != '-1')
    return n_actors, replay_ip


def check_connection(n_actors):
    """
    Wait for actors to connect before publishing params.
    """
    ctx = zmq.Context()
    socket = ctx.socket(zmq.ROUTER)
    socket.bind("tcp://*:52002")
    connected = set()
    finished = set()
    while True:
        identity, null, data = socket.recv_multipart()
        actor_id, signal = pickle.loads(data)
        socket.send_multipart((identity, null, b''))
        if signal == 1:
            connected.add(actor_id)
            print("Received handshake signal from actor {}".format(actor_id))
        else:
            finished.add(actor_id)
        if len(connected) == (n_actors + 1) and (len(connected) == len(finished)):
            # '+1' is needed to wait for evaluator to be connected
            break
    socket.close()
    ctx.term()
    print("Successfully connected with all actors!")
    return True


def send_param(param_queue):
    ctx = zmq.Context()
    socket = ctx.socket(zmq.PUB)
    socket.set_hwm(3)
    socket.bind("tcp://*:52001")
    while True:
        param = param_queue.get()
        for k, v in param.items():
            param[k] = v.cpu()
        data = pickle.dumps(param)
        socket.send(data, copy=False)
        param, data = None, None


def recv_batch(batch_queue, replay_ip, device):
    """
    receive batch from replay and transfer batch from cpu to gpu
    """
    def _thunk(thread_queue):
        ctx = zmq.Context.instance()
        socket = ctx.socket(zmq.DEALER)
        socket.setsockopt(zmq.IDENTITY, pickle.dumps('dealer-{}'.format(os.getpid())))
        socket.connect("tcp://{}:51003".format(replay_ip))
        outstanding = 0
        while True:
            socket.send(b'')
            outstanding += 1
            if outstanding < 3:
                try:
                    data = socket.recv(zmq.NOBLOCK, copy=False)
                except zmq.Again:
                    continue
            else:
                data = socket.recv(copy=False)
            thread_queue.put(data)
            outstanding -= 1
            data = None

    thread_queue = queue.Queue(maxsize=3)
    threading.Thread(target=_thunk, args=(thread_queue, )).start()

    while True:
        data = thread_queue.get()
        batch = pickle.loads(data)

        states, actions, rewards, next_states, dones, weights, idxes = batch
        states = np.array([np.array(state) for state in states])
        states = torch.FloatTensor(states).to(device)
        actions = torch.LongTensor(actions).to(device)
        rewards = torch.FloatTensor(rewards).to(device)
        next_states = np.array([np.array(state) for state in next_states])
        next_states = torch.FloatTensor(next_states).to(device)
        dones = torch.FloatTensor(dones).to(device)
        weights = torch.FloatTensor(weights).to(device)

        batch = [states, actions, rewards, next_states, dones, weights, idxes]
        batch_queue.put(batch)
        data, batch = None, None


def send_prios(prios_queue, replay_ip):
    ctx = zmq.Context()
    socket = ctx.socket(zmq.DEALER)
    socket.connect('tcp://{}:51002'.format(replay_ip))
    outstanding = 0
    max_outstanding = 16

    while True:
        idxes, prios = prios_queue.get()
        while outstanding >= max_outstanding:
            socket.recv()
            outstanding -= 1
        socket.send(pickle.dumps((idxes, prios)), copy=False)
        outstanding += 1
        idxes, prios = None, None


def train(args, n_actors, batch_queue, prios_queue, param_queue):
    env = wrapper.make_atari(args.env)
    env = wrapper.wrap_atari_dqn(env, args)
    utils.set_global_seeds(args.seed, use_torch=True)

    model = DuelingDQN(env).to(args.device)
    tgt_model = DuelingDQN(env).to(args.device)
    tgt_model.load_state_dict(model.state_dict())

    writer = SummaryWriter(comment="-{}-learner".format(args.env))
    # optimizer = torch.optim.Adam(model.parameters(), args.lr)
    optimizer = torch.optim.RMSprop(model.parameters(), args.lr, alpha=0.95, eps=1.5e-7, centered=True)

    check_connection(n_actors)

    param_queue.put(model.state_dict())
    learn_idx = 0
    ts = time.time()
    while True:
        *batch, idxes = batch_queue.get()
        loss, prios = utils.compute_loss(model, tgt_model, batch, args.n_steps, args.gamma)
        grad_norm = utils.update_parameters(loss, model, optimizer, args.max_norm)
        prios_queue.put((idxes, prios))
        batch, idxes, prios = None, None, None
        learn_idx += 1

        writer.add_scalar("learner/loss", loss, learn_idx)
        writer.add_scalar("learner/grad_norm", grad_norm, learn_idx)

        if learn_idx % args.target_update_interval == 0:
            print("Updating Target Network..")
            tgt_model.load_state_dict(model.state_dict())
        if learn_idx % args.save_interval == 0:
            print("Saving Model..")
            torch.save(model.state_dict(), "model.pth")
        if learn_idx % args.publish_param_interval == 0:
            param_queue.put(model.state_dict())
        if learn_idx % args.bps_interval == 0:
            bps = args.bps_interval / (time.time() - ts)
            print("Step: {:8} / BPS: {:.2f}".format(learn_idx, bps))
            writer.add_scalar("learner/BPS", bps, learn_idx)
            ts = time.time()


def main():
    n_actors, replay_ip = get_environ()
    args = argparser()

    # TODO: Need to adjust the maxsize of prios, param queue
    batch_queue = Queue(maxsize=args.queue_size)
    prios_queue = Queue(maxsize=args.prios_queue_size)
    param_queue = Queue(maxsize=3)
    procs = [
        Process(target=train, args=(args, n_actors, batch_queue, prios_queue, param_queue)),
        Process(target=send_param, args=(param_queue, )),
        Process(target=send_prios, args=(prios_queue, replay_ip)),
    ]

    for _ in range(args.n_recv_batch_process):
        p = Process(target=recv_batch, args=(batch_queue, replay_ip, args.device))
        procs.append(p)
    for p in procs:
        p.start()
    for p in procs:
        p.join()


if __name__ == '__main__':
    os.environ["OMP_NUM_THREADS"] = "1"
    mp.set_start_method("spawn")
    main()
