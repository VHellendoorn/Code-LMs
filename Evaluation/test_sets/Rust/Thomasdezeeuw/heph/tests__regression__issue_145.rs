//! The `TcpListener` and `TcpServer` should bind to port 0, using the same port
//! on each worker thread.

use std::io::{self, Read};
use std::net::SocketAddr;
use std::sync::{Arc, Mutex};
use std::thread;
use std::time::Duration;

use heph::actor::messages::Terminate;
use heph::net::{tcp, TcpListener, TcpServer, TcpStream};
use heph::rt::{Runtime, RuntimeRef, ThreadLocal};
use heph::supervisor::{NoSupervisor, Supervisor, SupervisorStrategy};
use heph::{actor, Actor, ActorOptions, ActorRef, NewActor};

const N: usize = 4;

#[test]
fn issue_145_tcp_server() {
    let mut runtime = Runtime::setup().num_threads(N).build().unwrap();

    let addresses = Arc::new(Mutex::new(Vec::<SocketAddr>::new()));
    let servers = Arc::new(Mutex::new(Vec::new()));
    let addr2 = addresses.clone();
    let srv2 = servers.clone();
    let conn_actor = (conn_actor as fn(_, _, _, _, _) -> _)
        .map_arg(move |(stream, address)| (stream, address, addr2.clone(), srv2.clone()));
    let address = "127.0.0.1:0".parse().unwrap();
    let server =
        TcpServer::setup(address, NoSupervisor, conn_actor, ActorOptions::default()).unwrap();
    let expected_address = server.local_addr();

    runtime
        .run_on_workers::<_, !>(move |mut runtime_ref| {
            let srv_ref = runtime_ref
                .try_spawn_local(ServerSupervisor, server, (), ActorOptions::default())
                .unwrap();
            // NOTE: this is not safe or supported. DO NOT USE THIS.
            let r = unsafe { std::mem::transmute_copy::<RuntimeRef, usize>(&runtime_ref) };
            servers.lock().unwrap().push((r, srv_ref));
            Ok(())
        })
        .unwrap();

    let handle = thread::spawn(move || {
        // TODO: replace with a barrier.
        thread::sleep(Duration::from_millis(50));

        for _ in 0..N {
            // Create a test connection to check the addresses.
            let mut stream = std::net::TcpStream::connect(&expected_address).unwrap();
            let mut buf = [0; 1];
            let n = stream.read(&mut buf).unwrap();
            assert_eq!(n, 0);

            // TODO: replace with a barrier.
            thread::sleep(Duration::from_millis(100));
        }
    });

    runtime.start().unwrap();

    handle.join().unwrap();
    for address in addresses.lock().unwrap().iter() {
        assert_eq!(*address, expected_address);
    }
}

struct ServerSupervisor;

impl<L, A> Supervisor<L> for ServerSupervisor
where
    L: NewActor<Message = tcp::server::Message, Argument = (), Actor = A, Error = io::Error>,
    A: Actor<Error = tcp::server::Error<!>>,
{
    fn decide(&mut self, _: tcp::server::Error<!>) -> SupervisorStrategy<()> {
        SupervisorStrategy::Stop
    }

    fn decide_on_restart_error(&mut self, _: io::Error) -> SupervisorStrategy<()> {
        SupervisorStrategy::Stop
    }

    fn second_restart_error(&mut self, _: io::Error) {}
}

#[allow(clippy::type_complexity)] // `servers` is too complex.
async fn conn_actor(
    mut ctx: actor::Context<!, ThreadLocal>,
    mut stream: TcpStream,
    _: SocketAddr,
    addresses: Arc<Mutex<Vec<SocketAddr>>>,
    servers: Arc<Mutex<Vec<(usize, ActorRef<tcp::server::Message>)>>>,
) -> Result<(), !> {
    let mut addresses = addresses.lock().unwrap();
    addresses.push(stream.local_addr().unwrap());

    // Shutdown the `TcpServer` that started us to ensure the next request goes
    // to a different server.
    // NOTE: this is not safe or supported. DO NOT USE THIS.
    let r = unsafe { std::mem::transmute_copy::<RuntimeRef, usize>(&*ctx.runtime()) };
    let mut servers = servers.lock().unwrap();
    let idx = servers.iter().position(|(rr, _)| r == *rr).unwrap();
    let (_, server_ref) = servers.remove(idx);
    server_ref.try_send(Terminate).unwrap();

    Ok(())
}

#[test]
fn issue_145_tcp_listener() {
    let mut runtime = Runtime::new().unwrap();
    runtime
        .run_on_workers::<_, !>(move |mut runtime_ref| {
            let actor = listener_actor as fn(_) -> _;
            runtime_ref
                .try_spawn_local(NoSupervisor, actor, (), ActorOptions::default())
                .unwrap();
            Ok(())
        })
        .unwrap();
    runtime.start().unwrap();
}

async fn listener_actor(mut ctx: actor::Context<!, ThreadLocal>) -> Result<(), !> {
    let address = "127.0.0.1:0".parse().unwrap();
    // NOTE: this should not fail.
    let mut listener = TcpListener::bind(&mut ctx, address).unwrap();
    let addr = listener.local_addr().unwrap();
    assert!(addr.port() != 0);
    Ok(())
}
