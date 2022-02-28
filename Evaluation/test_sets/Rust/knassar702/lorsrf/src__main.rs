mod args;
mod requester;
use crate::requester::*;
use crate::args::args;
use serde_json::json;
use indicatif::{ProgressBar, ProgressStyle};
use scoped_threadpool::Pool;
use std::{
    fs::File, 
    io::BufReader
};


fn main() {
    let the_args = args();
    let mut pool = Pool::new(the_args.value_of("threads").unwrap().parse().unwrap());
    let urls_file = File::open(the_args.value_of("targets").unwrap().to_string()).expect("file not found!");
    let _reader = BufReader::new(urls_file);
    let _requester = Requester {
        timeout:the_args.value_of("timeout").unwrap().parse().unwrap(),
        proxy:the_args.value_of("proxy").unwrap().to_string(),
        headers:extractheaders(the_args.value_of("headers").unwrap()),
        }.build();
    let params: Vec<String> = { 
        if the_args.is_present("wordlist") {
            convert_vec( BufReader::new(File::open(the_args.value_of("wordlist").unwrap()).expect("file not found ")) )
        } else {
            vec![String::from("")]
        }
    };

    let urls = convert_vec(_reader);
    let _prog = {
        if params.len() > 1 {
            let just = params.len() as u64 / urls.len() as u64;
            just / 14
        } else {
            urls.len() as u64
        }
    };
    let _bar = ProgressBar::new(_prog);
    _bar.set_style(ProgressStyle::default_bar()
        .template("[{elapsed_precise}] {bar:40.cyan/blue} {pos:>7}/{len:7} {msg}")
        .progress_chars("##-"));

    pool.scoped(|scope|{
        for _url in urls {

            let _urls = add_parameters(_url.clone().to_string(),the_args.value_of("host")
                                       .unwrap(),
                                       params.clone());
            for url in _urls {
                scope.execute(|| { 
                    let url = url ;
                    if the_args.is_present("post-only") == false {

                        match _requester.get(
                            url.clone()
                            .as_str()
                            .replace("%25METHOD%25","get")
                            .as_str()
                            ) {
                                Ok(_done) => {},
                                Err(_e) => {}
                            }

                    }

                    if the_args.is_present("json") == true {
                        match _requester.post({
                        if url.as_str().split_once("?") == None {
                             url.as_str()
                             }
                        else {
                            url.as_str().split_once("?").unwrap().0
                        }
                                }
                                              ,
                                            json!(query(url.clone()
                                                        .as_str().
                                                        replace("%25METHOD%25","post")
                                                        .as_str()
                                                        )
                                                  )
                                            .to_string()
                                              ) {
                                Ok(_done) => {},
                                Err(_e) => {}
                        }
                    }

                    if the_args.is_present("form") == true {
                        match _requester.post({
                        if url.as_str().split_once("?") == None {
                             url.as_str()
                             }
                        else {
                            url.as_str().split_once("?").unwrap().0
                        }
                                }                                              ,
                                              extract_params(url
                                                             .split_once("?")
                                                             .unwrap().0,query(url.clone().as_str()
                                                                               .replace("%25METHOD%25","post")
                                                                               .as_str()
                                                                               )
                                                             )
                                              ) {
                                Ok(_done) => {},
                                Err(_e) => {}
                        }
                    }
                    _bar.inc(1);




                });
            }
        }
        });
}

