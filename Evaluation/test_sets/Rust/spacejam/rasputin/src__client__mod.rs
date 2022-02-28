use std::collections::BTreeMap;
use std::io::{self, Error, ErrorKind};
use std::net::SocketAddr;
use std::sync::mpsc::channel;

use bytes::{Buf, ByteBuf};
use threadpool::ThreadPool;
use protobuf::{self, Message};
use mio::{TryRead, TryWrite};
use mio::tcp::TcpStream;

use {CliReq, CliRes, GetReq, GetRes, RangeBounds, RedirectRes, SetReq,
     SetRes, Version, CASReq, CASRes, DelReq, DelRes};
use codec::{self, Codec, Framed};

pub struct Client {
    servers: Vec<SocketAddr>,
    ranges: BTreeMap<RangeBounds, SocketAddr>,
    pool: ThreadPool,
    req_counter: u64,
}

impl Client {
    pub fn new(servers: Vec<SocketAddr>, nthreads: usize) -> Client {
        Client {
            servers: servers,
            ranges: BTreeMap::new(),
            pool: ThreadPool::new(nthreads),
            req_counter: 0,
        }
    }

    fn get_id(&mut self) -> u64 {
        self.req_counter += 1;
        self.req_counter
    }

    pub fn set<'a>(
        &mut self,
        key: &'a [u8],
        value: &'a [u8]
    ) -> io::Result<SetRes> {

        let mut set = SetReq::new();
        set.set_key(key.to_vec());
        set.set_value(value.to_vec());
        let mut req = CliReq::new();
        req.set_set(set);
        req.set_req_id(self.get_id());

        self.req(key.to_vec(), req).map(|cli_res| {
            let set_res = cli_res.get_set();
            debug!("got response success: {} txid: {} err: {}",
                     set_res.get_success(),
                     set_res.get_txid(),
                     set_res.get_err());
            cli_res.get_set().clone()
        })
    }

    pub fn get<'a>(
        &mut self,
        key: &'a [u8],
    ) -> io::Result<GetRes> {

        let mut get = GetReq::new();
        get.set_key(key.to_vec());
        let mut req = CliReq::new();
        req.set_get(get);
        req.set_req_id(self.get_id());

        self.req(key.to_vec(), req).map(|cli_res| {
            let get_res = cli_res.get_get();
            debug!("got response success: {} txid: {} err: {}",
                     get_res.get_success(),
                     get_res.get_txid(),
                     get_res.get_err());
            cli_res.get_get().clone()
        })
    }

    pub fn cas<'a>(
        &mut self,
        key: &'a [u8],
        old_value: &'a [u8],
        new_value: &'a [u8]
    ) -> io::Result<CASRes> {

        let mut cas = CASReq::new();
        cas.set_key(key.to_vec());
        cas.set_old_value(old_value.to_vec());
        cas.set_new_value(new_value.to_vec());
        let mut req = CliReq::new();
        req.set_cas(cas);
        req.set_req_id(self.get_id());

        self.req(key.to_vec(), req).map(|cli_res| {
            let cas_res = cli_res.get_cas();
            debug!("got response success: {} txid: {} err: {}",
                     cas_res.get_success(),
                     cas_res.get_txid(),
                     cas_res.get_err());
            cli_res.get_cas().clone()
        })
    }

    pub fn del<'a>(
        &mut self,
        key: &'a [u8],
    ) -> io::Result<DelRes> {

        let mut del = DelReq::new();
        del.set_key(key.to_vec());
        let mut req = CliReq::new();
        req.set_del(del);
        req.set_req_id(self.get_id());

        self.req(key.to_vec(), req).map(|cli_res| {
            let del_res = cli_res.get_del();
            debug!("got response success: {} txid: {} err: {}",
                     del_res.get_success(),
                     del_res.get_txid(),
                     del_res.get_err());
            cli_res.get_del().clone()
        })
    }

    fn req(&mut self, key: Vec<u8>, req: CliReq) -> io::Result<CliRes> {
        // send to a peer, they'll redirect us if we're wrong
        for peer in self.servers.iter() {
            debug!("trying peer {:?}", peer);
            let mut stream_attempt = TcpStream::connect(&peer);
            if stream_attempt.is_err() {
                continue;
            }

            let mut stream = stream_attempt.unwrap();
            let mut codec = Framed::new();
            let mut msg =
                codec.encode(ByteBuf::from_slice(&*req.write_to_bytes()
                                                      .unwrap()));

            if send_to(&mut stream, &mut msg).is_err() {
                debug!("could not send");
                continue;
            }
            match recv_into(&mut stream, &mut codec) {
                Ok(res_buf) => {
                    let res: &[u8] = res_buf.bytes();
                    let cli_res: CliRes = protobuf::parse_from_bytes(res)
                                              .unwrap();
                    if cli_res.has_redirect() {
                        debug!("we got redirect to {}!",
                                 cli_res.get_redirect().get_address());
                        // TODO(tyler) try redirected host next
                        continue;
                    }
                    return Ok(cli_res);
                }
                Err(e) => {
                    debug!("got err on recv_into: {}", e);
                    continue;
                }
            }
        }
        Err(Error::new(ErrorKind::Other, "unable to reach any servers!"))
    }
}

fn send_to(stream: &mut TcpStream, buf: &mut ByteBuf) -> io::Result<()> {
    loop {
        match stream.try_write_buf(buf) {
            Ok(None) => {
                continue;
            }
            Ok(Some(r)) => {
                if buf.remaining() == 0 {
                    return Ok(());
                }
            }
            Err(e) => {
                match e.raw_os_error() {
                    Some(32) => {
                        debug!("client disconnected");
                    }
                    Some(e) =>
                        debug!("not implemented; client os err={:?}", e),
                    _ => debug!("not implemented; client err={:?}", e),
                };
                // Don't reregister.
                return Err(e);
            }
        }
    }
}

fn recv_into<T>(stream: &mut TcpStream,
                codec: &mut Codec<ByteBuf, T>)
                -> io::Result<T> {
    loop {
        let mut res_buf = ByteBuf::mut_with_capacity(1024);
        match stream.try_read_buf(&mut res_buf) {
            Ok(None) => {
                //debug!("got readable, but can't read from the socket");
            }
            Ok(Some(r)) => {
                //debug!("CONN : we read {} bytes!", r);
            }
            Err(e) => {
                debug!("not implemented; client err={:?}", e);
            }
        }
        let mut r: Vec<T> = codec.decode(&mut res_buf.flip());
        if r.len() == 1 {
            let res_buf = r.pop().unwrap();
            return Ok(res_buf)
        }
    }
}
