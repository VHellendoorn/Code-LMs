extern crate futures;
extern crate futures_util;

use futures::prelude::*;
use futures::channel::{mpsc, oneshot};
use futures::executor::block_on;
use futures::future::{ok, err, join_all, select_all, poll_fn};
use futures::stream::iter_result;
use futures_util::stream::select_all as select_all_stream;

use std::thread;

const FINISHED: Result<Async<()>, Never> = Ok(Async::Ready(()));

fn join_all_example() {
    let future1 = Ok::<_, ()>(vec![1, 2, 3]);
    let future2 = Ok(vec![10, 20, 30]);
    let future3 = Ok(vec![100, 200, 300]);

    let results = block_on(join_all(vec![future1, future2, future3])).unwrap();
    println!("Results of joining 3 futures: {:?}", results);

    // For parameters with a lifetime
    fn sum_vecs<'a>(vecs: Vec<&'a [i32]>) -> Box<Future<Item = Vec<i32>, Error = ()> + 'static> {
        Box::new(join_all(vecs.into_iter().map(|x| Ok::<i32, ()>(x.iter().sum()))))
    }

    let sum_results = block_on(sum_vecs(vec![&[1, 3, 5], &[6, 7, 8], &[0]])).unwrap();
    println!("sum_results: {:?}", sum_results);
}

fn shared() {
    let thread_number = 2;
    let (tx, rx) = oneshot::channel::<u32>();
    let f = rx.shared();
    let threads = (0..thread_number)
        .map(|thread_index| {
            let cloned_f = f.clone();
            thread::spawn(move || {
                let value = block_on(cloned_f).unwrap();
                println!("Thread #{}: {:?}", thread_index, *value);
            })
        })
        .collect::<Vec<_>>();
    tx.send(42).unwrap();

    let shared_return = block_on(f).unwrap();
    println!("shared_return: {:?}", shared_return);

    for f in threads {
        f.join().unwrap();
    }
}

fn select_all_example() {
    let vec = vec![ok(3), err(24), ok(7), ok(9)];

    let (value, _, vec) = block_on(select_all(vec)).unwrap();
    println!("Value of vec: = {}", value);

    let (value, _, vec) = block_on(select_all(vec)).err().unwrap();
    println!("Value of vec: = {}", value);

    let (value, _, vec) = block_on(select_all(vec)).unwrap();
    println!("Value of vec: = {}", value);

    let (value, _, _) = block_on(select_all(vec)).unwrap();
    println!("Value of vec: = {}", value);

    let (tx_1, rx_1) = mpsc::unbounded::<u32>();
    let (tx_2, rx_2) = mpsc::unbounded::<u32>();
    let (tx_3, rx_3) = mpsc::unbounded::<u32>();

    let streams = vec![rx_1, rx_2, rx_3];
    let stream = select_all_stream(streams);

    tx_1.unbounded_send(3).unwrap();
    tx_2.unbounded_send(6).unwrap();
    tx_3.unbounded_send(9).unwrap();

    let (value, details) = block_on(stream.next()).unwrap();

    println!("value for select_all on streams: {:?}", value);
    println!("stream details: {:?}", details);
}

fn flatten() {
    let f = ok::<_, _>(ok::<u32, Never>(100));
    let f = f.flatten();
    let results = block_on(f).unwrap();
    println!("results: {}", results);
}

fn fuse() {
    let mut f = ok::<u32, Never>(123).fuse();

    block_on(poll_fn(move |mut cx| {
            let first_result = f.poll(&mut cx);
            let second_result = f.poll(&mut cx);
            let third_result = f.poll(&mut cx);

            println!("first result: {:?}", first_result);
            println!("second result: {:?}", second_result);
            println!("third result: {:?}", third_result);

            FINISHED
        }))
        .unwrap();
}

fn inspect() {
    let f = ok::<u32, Never>(111);
    let f = f.inspect(|&val| println!("inspecting: {}", val));
    let results = block_on(f).unwrap();
    println!("results: {}", results);
}

fn chaining() {
    let (tx, rx) = mpsc::channel(3);
    let f = tx.send(1)
        .and_then(|tx| tx.send(2))
        .and_then(|tx| tx.send(3));

    let t = thread::spawn(move || {
        block_on(f.into_future()).unwrap();
    });

    t.join().unwrap();

    let result: Vec<_> = block_on(rx.collect()).unwrap();
    println!("Result from chaining and_then: {:?}", result);

    // Chaining streams together
    let stream1 = iter_result(vec![Ok(10), Err(false)]);
    let stream2 = iter_result(vec![Err(true), Ok(20)]);

    let stream = stream1.chain(stream2)
        .then(|result| Ok::<_, ()>(result));

    let result: Vec<_> = block_on(stream.collect()).unwrap();
    println!("Result from chaining our streams together: {:?}", result);
}

fn main() {
    println!("join_all_example():");
    join_all_example();

    println!("\nshared():");
    shared();

    println!("\nselect_all_example():");
    select_all_example();

    println!("\nflatten():");
    flatten();

    println!("\nfuse():");
    fuse();

    println!("\ninspect():");
    inspect();

    println!("\nchaining():");
    chaining();
}
