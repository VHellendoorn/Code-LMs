mod transport;
mod hoster_manager;
mod stats_conduit;
mod id_generator;

use std::sync::{Arc, Mutex};
use std::collections::HashMap;
use warp::{self, Filter};
use warp::http::{Response, Uri};
use warp::path::FullPath;
use hoster_manager::HosterManager;
use futures::{Future, Stream};
use futures::sync::{mpsc};
use futures::future::Either;
use clap::{App, Arg};
use std::net::SocketAddrV4;
use hyper::rt;
use crate::id_generator::{create_generator};

type HosterManagers = Arc<Mutex<HashMap<String, HosterManager>>>;


fn main() {
    let matches = App::new("fibridge proxy")
        .about("Share local files via HTTP streaming")
        .arg(Arg::with_name("host")
             .short("h")
             .long("host")
             .value_name("HOST")
             .takes_value(true))
        .arg(Arg::with_name("ip")
             .short("i")
             .long("ip-address")
             .value_name("IP")
             .takes_value(true))
        .arg(Arg::with_name("port")
             .short("p")
             .long("port")
             .value_name("PORT")
             .takes_value(true))
        .arg(Arg::with_name("id-type")
             .long("id-type")
             .value_name("ID TYPE")
             .takes_value(true))
        .arg(Arg::with_name("key")
             .long("key")
             .value_name("TLS_KEY")
             .takes_value(true))
        .arg(Arg::with_name("cert")
             .long("cert")
             .value_name("TLS_CERT")
             .takes_value(true))
        .arg(Arg::with_name("secure-port")
             .long("secure-port")
             .value_name("SECURE_PORT")
             .takes_value(true))
        .get_matches();

    let port = matches.value_of("port").unwrap_or("9001");
    let ip = matches.value_of("ip").unwrap_or("127.0.0.1");
    let id_type = matches.value_of("id-type").unwrap_or("short-code");
    let addr = format!("{}:{}", ip, port);

    let hoster_managers = Arc::new(Mutex::new(HashMap::new()));
    let hoster_managers_clone = hoster_managers.clone();
    let range_clone = hoster_managers.clone();
    let done_clone = hoster_managers.clone();

    let (done_tx, done_rx) = mpsc::unbounded::<String>();

    let done_stream = done_rx.for_each(move |done_id| {
        done_clone.lock().expect("get lock").remove(&done_id);
        Ok(())
    }).map_err(|_| ());

    let id_generator = Arc::new(create_generator(id_type));

    let omnis = warp::path("omnistreams")
        .map(move || hoster_managers.clone())
        .and(warp::ws2())
        .map(move |hoster_managers: HosterManagers, ws: warp::ws::Ws2| {

            let done_tx = done_tx.clone();
            let id_generator = id_generator.clone();

            ws.on_upgrade(move |socket| {
                
                let mut id = id_generator.gen();

                // TODO: this is pretty hacky
                let mut id_attempts = 0;
                while hoster_managers.lock().expect("get lock").get(&id).is_some() {
                    id = id_generator.gen();
                    id_attempts += 1;
                    if id_attempts > 1000 {
                        panic!("Out of ids");
                    }
                }

                let hoster = HosterManager::new(id.to_string(), socket, done_tx);

                hoster_managers.lock().expect("get lock").insert(hoster.id(), hoster);

                // TODO: eventually need to actually remove the old ones
                dbg!(hoster_managers.lock().expect("get lock").keys());

                futures::future::ok(())
            })
        });

    // TODO: reduce duplication with non_ranged below
    let ranged = warp::header::<String>("Range")
        .and(warp::path::param())
        .and(warp::path::param())
        .and_then(move |range, id: String, filename: String| {
            let mut lock = range_clone.lock().expect("get lock");

            println!("GET /{}/{} {}", id, filename, range);

            match lock.get_mut(&id) {
                Some(manager) => {
                    Either::A(manager.process_request(filename, range)
                        .map_err(|_e| warp::reject::not_found()))
                },
                None => {
                    // TODO: This still feels super hacky. There's got to be some way to have these
                    // all be part of the same future.
                    Either::B(futures::future::ok(Response::builder()
                            .status(404)
                            .body("Not found".into())
                            .expect("error response")))
                },
            }
        });

    let non_ranged = warp::path::param()
        .and(warp::path::param())
        .and_then(move |id: String, filename: String| {

            println!("GET /{}/{}", id, filename);

            let mut lock = hoster_managers_clone.lock().expect("get lock");

            match lock.get_mut(&id) {
                Some(manager) => {
                    Either::A(manager.process_request(filename, "".to_string())
                        .map_err(|_e| warp::reject::not_found()))
                },
                None => {
                    // TODO: This still feels super hacky. There's got to be some way to have these
                    // all be part of the same future.
                    Either::B(futures::future::ok(Response::builder()
                            .status(404)
                            .body("Not found".into())
                            .expect("error response")))
                },
            }
        });

    let download = warp::get2().and(ranged.or(non_ranged));

    let index = warp::path::end().map(|| {
        warp::reply::html(include_str!("../../fibridge-gui-js/dist/index.html"))
    });

    let routes = index
        .or(omnis)
        .or(download);


    let key = matches.value_of("key");
    let cert = matches.value_of("cert");


    if key.is_some() && cert.is_some() {

        let secure_port = matches.value_of("secure-port").unwrap_or("9002");
        
        // if host was specified, use that value, otherwise use the ip
        let host = matches.value_of("host").unwrap_or(ip);
        let secure_redirect_link = format!("{}:{}", host, secure_port);

        // redirect http to https
        let http_routes = warp::path::full().map(move |path: FullPath| {
            let uri = Uri::builder()
                .scheme("https")
                .authority(secure_redirect_link.as_str())
                .path_and_query(path.as_str())
                .build()
                .expect("parse uri");
            warp::redirect(uri) 
        });
        let http_server_future = warp::serve(http_routes)
            .bind(addr.parse::<SocketAddrV4>()
            .expect("parse address"));

        let secure_addr = format!("{}:{}", ip, secure_port);
        let https_server_future = warp::serve(routes)
            .tls(cert.unwrap(), key.unwrap())
            .bind(secure_addr.parse::<SocketAddrV4>().expect("parse address"));
        rt::run(rt::lazy(|| {
            rt::spawn(done_stream);
            rt::spawn(http_server_future);
            rt::spawn(https_server_future);
            Ok(())
        }));
    }
    else {
        let server_future = warp::serve(routes).bind(addr.parse::<SocketAddrV4>().expect("parse address"));
        rt::run(rt::lazy(|| {
            rt::spawn(done_stream);
            rt::spawn(server_future);
            Ok(())
        }));
    }
}
