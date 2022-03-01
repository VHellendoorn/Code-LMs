import argparse
import numpy as np
import torch
import os
from tensorboardX import SummaryWriter
from environment import Environment
from network import Net, Agent
from memory import Memory
import config as p
from utils import utils

shape_ref_type=''
shape_category=''
shape_ref_path=''
shape_vox_path=''
edgeloop_path=''
save_net_path=''
save_net_f = 0
save_log_path=''
save_tmp_result_path=''

def parse_args():

    """parse input arguments"""
    parser = argparse.ArgumentParser(description='Mesh-Agent')
    
    parser.add_argument('--gpu', type=str, default='0', help='which gpu to use')
    parser.add_argument('--reference', type=str, default='depth', help='type of shape reference, rgb or depth image')
    parser.add_argument('--category', type=str, default='airplane-02691156', help='category name, should be consistent with the name used in file path')
    parser.add_argument('--data_root', type=str, default='../data/', help='root directory of all the data')
    
    parser.add_argument('--save_net', type=str, default='../save_para/', help='directory to save the network parameters')
    parser.add_argument('--save_net_f', type=int, default=300, help='frequency to save the network parameters (number of episode)')
    parser.add_argument('--save_log', type=str, default='../log/', help='directory to save the training log (tensorboard)')
    parser.add_argument('--save_tmp_result', type=str, default='../tmp/', help='directory to save the temporary results during training')


    args = parser.parse_args()
    
    return args
    

def imitation_learning(agent, env, writer, shape_list):
    
    episode_count = 0
    for epoch in range(p.DAGGER_EPOCH):
        for shape_count in range(len(shape_list)):

            shape_name=shape_list[shape_count]
            vox_l_fn = shape_vox_path+ shape_name+'-16.binvox'
            vox_h_fn = shape_vox_path+ shape_name+'-64.binvox'
            prim_mesh_fn = edgeloop_path+shape_name+'.obj'
            loop_info_fn = edgeloop_path+shape_name+'.loop'
            ref_fn = shape_ref_path + shape_name + '.png'
            
            shape_infopack=[shape_name, vox_l_fn, vox_h_fn,  prim_mesh_fn, loop_info_fn, shape_ref_type, ref_fn]
            
            for episode in range(p.DAGGER_ITER):
                            
                print('Shape', shape_name, 'Dagger episode', episode)
                
                valid, s, loop, step = env.reset(shape_infopack)
                if not valid:
                    continue
                
                episode_count += 1
                agent.memory_self.clear()
                acm_r=0
                            
                while True:
                    
                    valid_mask = env.get_valid_action_mask()
                    
                    #poll the expert
                    a = env.get_virtual_expert_action(valid_mask)
                    s_, loop_, step_, r, done = env.next_no_update(a)
                    expert_action= env.action_map[a]
                    
                    agent.memory_long.store(s, loop, step, a, r, s_, loop_, step_)
                    agent.memory_self.store(s, loop, step, a, r, s_, loop_, step_)
                                                        
                    #update the state
                    if episode!=0:
                        a = agent.choose_action(s, loop, step, valid_mask, 1.0)                
                    real_action = env.action_map[a]
                    s_, loop_, step_, r, done = env.next(a)
                    
                    acm_r+=r
                    
                    if done:
                        # log_info='IL_'+str(epoch)+'_shape_'+str(shape_count)+'_epi_'+str(episode)+'_r_'+str(format(acm_r, '.4f'))+'_'+shape_name
                        # env.output_result(log_info, save_tmp_result_path)
                        writer.add_scalar('Mesh_IL/'+shape_category, acm_r, episode_count)
                        break
                    
                    s = s_
                    loop = loop_    
                    step = step_
                
                print('reward:', acm_r)
                
                for learn in range(p.DAGGER_LEARN):
                    agent.learn(learning_mode=2, is_ddqn=True)
                                

def reinforcement_learning(agent, env, writer, shape_list):
    
    episode_count = 0
    for epoch in range(p.RL_EPOCH):
        for shape_count in range(len(shape_list)):
            
            shape_name=shape_list[shape_count]
            vox_l_fn = shape_vox_path+ shape_name+'-16.binvox'
            vox_h_fn = shape_vox_path+ shape_name+'-64.binvox'
            prim_mesh_fn = edgeloop_path+shape_name+'.obj'
            loop_info_fn = edgeloop_path+shape_name+'.loop'
            ref_fn = shape_ref_path + shape_name + '.png'
            
            shape_infopack=[shape_name, vox_l_fn, vox_h_fn,  prim_mesh_fn, loop_info_fn, shape_ref_type, ref_fn]
            
            print('Shape:', shape_count, 'RL epoch:', epoch)
            
            valid, s, loop, step = env.reset(shape_infopack)
            if not valid:
                continue
            
            episode_count+=1
            acm_r=0
            
            while True:
                
                valid_mask = env.get_valid_action_mask()
                a = agent.choose_action(s, loop, step, valid_mask, p.EPSILON)
                s_, loop_, step_, r, done = env.next(a)
                
                agent.memory_self.store(s, loop, step, a, r, s_, loop_, step_)
                
                acm_r+=r
                
                if agent.memory_self.memory_counter >= p.MEMORY_SELF_CAPACITY:
                    agent.learn(learning_mode=3, is_ddqn=True)
                
                if done:
                    # log_info='RL_'+str(epoch)+'_shape_'+str(shape_count)+'_r_'+str(format(acm_r, '.4f'))+'_'+shape_name
                    # env.output_result(log_info, save_tmp_result_path)
                    writer.add_scalar('Mesh_RL/'+args.category, acm_r, episode_count)
                    break
                
                s = s_
                loop = loop_    
                step = step_
            
            if episode_count % save_net_f == 0:
                torch.save(agent.eval_net.state_dict(), save_net_path+'eval_RL_'+ shape_ref_type + '_'+ shape_category + '.pth')
                torch.save(agent.target_net.state_dict(),  save_net_path+'target_RL_'+ shape_ref_type + '_'+ shape_category + '.pth')

            
if __name__ == "__main__":
    
    args = parse_args()
    
    shape_ref_type=args.reference
    shape_category=args.category
    shape_ref_path=args.data_root + 'shape_reference/'+shape_ref_type+'/'+shape_category + '/'
    shape_vox_path=args.data_root + 'shape_binvox/' + shape_category + '/'
    edgeloop_path=args.data_root + 'prim_result/' + shape_ref_type + '/'+ shape_category + '/'
    
    IL_shapelist_path=args.data_root + 'shape_list/' + shape_category +'/' + shape_category + '-demo.txt'
    RL_shapelist_path=args.data_root + 'shape_list/' + shape_category +'/' + shape_category + '-train.txt'
  
    save_net_path=args.save_net
    save_net_f = args.save_net_f
    save_log_path= args.save_log
    save_tmp_result_path= args.save_tmp_result
    
    utils.check_dirs([save_net_path, save_log_path, save_tmp_result_path])

    #GPU
    os.environ['CUDA_VISIBLE_DEVICES'] = args.gpu
    
    #shape list
    IL_shape_list=utils.load_filelist(IL_shapelist_path)
    RL_shape_list=utils.load_filelist(RL_shapelist_path)

    #initialize
    env = Environment()
    agent = Agent()
    writer = SummaryWriter(save_log_path)

    
    imitation_learning(agent, env, writer, IL_shape_list)
    torch.save(agent.eval_net.state_dict(), save_net_path+'eval_IL_'+ shape_ref_type + '_'+ shape_category + '.pth')
    torch.save(agent.target_net.state_dict(),  save_net_path+'target_IL_'+ shape_ref_type + '_'+ shape_category + '.pth')
    agent.memory_self.clear()

    reinforcement_learning(agent, env, writer, RL_shape_list)
    torch.save(agent.eval_net.state_dict(), save_net_path+'eval_RL_'+ shape_ref_type + '_'+ shape_category + '.pth')
    torch.save(agent.target_net.state_dict(),  save_net_path+'target_RL_'+ shape_ref_type + '_'+ shape_category + '.pth')

