#[macro_use]
extern crate clap;
#[macro_use]
extern crate lazy_static;
extern crate rayon;
extern crate rand;
extern crate regex;
extern crate secp256k1;
extern crate tiny_keccak;
extern crate num_cpus;
extern crate termcolor;
#[macro_use]
extern crate generic_array;
extern crate typenum;

#[macro_use]
mod macros;
mod patterns;

use patterns::{Patterns, StringPatterns, RegexPatterns};
use std::fmt::Write;
use std::sync::Mutex;
use std::thread;
use std::sync::Arc;
use std::time::Duration;
use clap::{Arg, ArgMatches};
use rand::OsRng;
use regex::Regex;
use secp256k1::Secp256k1;
use termcolor::{Color, ColorChoice, Buffer, BufferWriter};
use typenum::U40;

type AddressLengthType = U40;

const ADDRESS_LENGTH: usize = 40;
const ADDRESS_BYTES: usize = ADDRESS_LENGTH / 2;
const KECCAK_OUTPUT_BYTES: usize = 32;
const ADDRESS_BYTE_INDEX: usize = KECCAK_OUTPUT_BYTES - ADDRESS_BYTES;

lazy_static! {
    static ref ADDRESS_PATTERN: Regex = Regex::new(r"^[0-9a-f]{1,40}$").unwrap();
}

struct BruteforceResult {
    address: String,
    private_key: String,
}

fn parse_color_choice(string: &str) -> Result<ColorChoice, ()> {
    Ok(match string {
           "always" => ColorChoice::Always,
           "always_ansi" => ColorChoice::AlwaysAnsi,
           "auto" => ColorChoice::Auto,
           "never" => ColorChoice::Never,
           _ => return Err(()),
       })
}

fn to_hex_string(slice: &[u8], expected_string_size: usize) -> String {
    let mut result = String::with_capacity(expected_string_size);

    for &byte in slice {
        write!(&mut result, "{:02x}", byte).expect("Unable to format the public key.");
    }

    result
}

fn main() {
    let matches = app_from_crate!()
        .arg(Arg::with_name("regexp")
             .long("regexp")
             .short("e")
             .help("Use regex pattern matching")
             .long_help("By default, an address is accepted when the beginning matches one of the
strings provided as the patterns. This flag changes the functionality from
plain string matching to regex pattern matching."))
        .arg(Arg::with_name("quiet")
             .long("quiet")
             .short("q")
             .help("Output only the results")
             .long_help("Output only the resulting address and private key separated by a space."))
        .arg(Arg::with_name("color")
             .long("color")
             .short("c")
             .help("Changes the color formatting strategy")
             .long_help("Changes the color formatting strategy in the following way:
    always      -- Try very hard to emit colors. This includes
                   emitting ANSI colors on Windows if the console
                   API is unavailable.
    always_ansi -- like always, except it never tries to use
                   anything other than emitting ANSI color codes.
    auto        -- Try to use colors, but don't force the issue.
                   If the console isn't available on Windows, or
                   if TERM=dumb, for example, then don't use colors.
    never       -- Never emit colors.\n")
             .takes_value(true)
             .possible_values(&["always", "always_ansi", "auto", "never"])
             .default_value("auto"))
        .arg(Arg::with_name("stream")
             .long("stream")
             .short("s")
             .help("Keep outputting results")
             .long_help("Instead of outputting a single result, keep outputting until terminated."))
        .arg(Arg::with_name("PATTERN")
             .help("The pattern to match the address against")
             .long_help("The pattern to match the address against.
If no patterns are provided, they are read from the stdin (standard input),
where each pattern is on a separate line.
Addresses are outputted if the beginning matches one of these patterns.
If the `--regexp` flag is used, the addresses are matched against these
patterns as regex patterns, which replaces the basic string comparison.")
             .multiple(true))
        .get_matches();

    let quiet = matches.is_present("quiet");
    let color_choice = parse_color_choice(matches.value_of("color").unwrap()).unwrap();
    let buffer_writer = Arc::new(Mutex::new(BufferWriter::stdout(color_choice)));

    if matches.is_present("regexp") {
        let patterns = Arc::new(RegexPatterns::new(buffer_writer.clone(), &matches));

        main_pattern_type_selected(matches, quiet, buffer_writer, patterns);
    } else {
        let patterns = Arc::new(StringPatterns::new(buffer_writer.clone(), &matches));

        main_pattern_type_selected(matches, quiet, buffer_writer, patterns);
    };
}

fn main_pattern_type_selected<P: Patterns + 'static>(matches: ArgMatches,
                                                     quiet: bool,
                                                     buffer_writer: Arc<Mutex<BufferWriter>>,
                                                     patterns: Arc<P>) {
    if patterns.len() <= 0 {
        let mut stdout = buffer_writer.lock().unwrap().buffer();
        cprintln!(false,
                  stdout,
                  Color::Red,
                  "Please, provide at least one valid pattern.");
        buffer_writer
            .lock()
            .unwrap()
            .print(&stdout)
            .expect("Could not write to stdout.");
        std::process::exit(1);
    }

    {
        let mut stdout = buffer_writer.lock().unwrap().buffer();
        cprintln!(quiet,
                  stdout,
                  Color::White,
                  "---------------------------------------------------------------------------------------");

        if patterns.len() <= 1 {
            cprint!(quiet,
                    stdout,
                    Color::White,
                    "Looking for an address matching ");
        } else {
            cprint!(quiet,
                    stdout,
                    Color::White,
                    "Looking for an address matching any of ");
        }

        cprint!(quiet, stdout, Color::Cyan, "{}", patterns.len());

        if patterns.len() <= 1 {
            cprint!(quiet, stdout, Color::White, " pattern");
        } else {
            cprint!(quiet, stdout, Color::White, " patterns");
        }

        cprintln!(quiet, stdout, Color::White, "");
        cprintln!(quiet,
                  stdout,
                  Color::White,
                  "---------------------------------------------------------------------------------------");
        buffer_writer
            .lock()
            .unwrap()
            .print(&stdout)
            .expect("Could not write to stdout.");
    }

    let thread_count = num_cpus::get();

    loop {
        let mut threads = Vec::with_capacity(thread_count);
        let result: Arc<Mutex<Option<BruteforceResult>>> = Arc::new(Mutex::new(None));
        let iterations_this_second: Arc<Mutex<u32>> = Arc::new(Mutex::new(0));
        let alg = Arc::new(Secp256k1::new());
        let working_threads = Arc::new(Mutex::new(thread_count));

        for _ in 0..thread_count {
            let working_threads = working_threads.clone();
            let patterns = patterns.clone();
            let result = result.clone();
            let alg = alg.clone();
            let iterations_this_second = iterations_this_second.clone();

            threads.push(thread::spawn(move || {
                'dance:
                loop {
                    {
                        let result_guard = result.lock().unwrap();

                        if let Some(_) = *result_guard {
                            break 'dance;
                        }
                    }

                    let mut rng = OsRng::new()
                        .expect("Could not create a secure random number generator. Please file a GitHub issue.");
                    let (private_key, public_key) = alg.generate_keypair(&mut rng)
                        .expect("Could not generate a random keypair. Please file a GitHub issue.");
                    let public_key_array = &public_key.serialize_vec(&alg, false)[1..];
                    let keccak = tiny_keccak::keccak256(public_key_array);
                    let address = to_hex_string(&keccak[ADDRESS_BYTE_INDEX..], 40);  // get rid of the constant 0x04 byte

                    if patterns.contains(&address) {
                        *result.lock().unwrap() = Some(BruteforceResult {
                            address,
                            private_key: to_hex_string(&private_key[..], 64),
                        });
                        break 'dance;
                    }

                    *iterations_this_second.lock().unwrap() += 1;
                }

                *working_threads.lock().unwrap() -= 1;
            }));
        }

        // Note:
        // Buffers are intended for correct concurrency.
        let sync_buffer: Arc<Mutex<Option<Buffer>>> = Arc::new(Mutex::new(None));

        {
            let buffer_writer = buffer_writer.clone();
            let sync_buffer = sync_buffer.clone();
            let result = result.clone();

            thread::spawn(move || 'dance: loop {
                              thread::sleep(Duration::from_secs(1));

                              {
                                  let result_guard = result.lock().unwrap();

                                  if let Some(_) = *result_guard {
                                      break 'dance;
                                  }
                              }

                              let mut buffer = buffer_writer.lock().unwrap().buffer();
                              let mut iterations_per_second =
                                  iterations_this_second.lock().unwrap();
                              cprint!(quiet, buffer, Color::Cyan, "{}", *iterations_per_second);
                              cprintln!(quiet, buffer, Color::White, " addresses / second");
                              *sync_buffer.lock().unwrap() = Some(buffer);
                              *iterations_per_second = 0;
                          });
        }

        'dance: loop {
            if *working_threads.lock().unwrap() <= 0 {
                break 'dance;
            }

            if let Some(ref buffer) = *sync_buffer.lock().unwrap() {
                buffer_writer
                    .lock()
                    .unwrap()
                    .print(buffer)
                    .expect("Could not write to stdout.");
            }

            *sync_buffer.lock().unwrap() = None;

            thread::sleep(Duration::from_millis(10));
        }

        for thread in threads {
            thread.join().unwrap();
        }

        let result = result.lock().unwrap();
        let result = result.as_ref().unwrap();

        {
            let mut stdout = buffer_writer.lock().unwrap().buffer();
            cprintln!(quiet,
                      stdout,
                      Color::White,
                      "---------------------------------------------------------------------------------------");
            cprint!(quiet, stdout, Color::White, "Found address: ");
            cprintln!(quiet, stdout, Color::Yellow, "0x{}", result.address);
            cprint!(quiet, stdout, Color::White, "Generated private key: ");
            cprintln!(quiet, stdout, Color::Red, "{}", result.private_key);
            cprintln!(quiet,
                      stdout,
                      Color::White,
                      "Import this private key into an ethereum wallet in order to use the address.");
            cprintln!(quiet,
                      stdout,
                      Color::Green,
                      "Buy me a cup of coffee; my ethereum address: 0xc0ffee3bd37d408910ecab316a07269fc49a20ee");
            cprintln!(quiet,
                      stdout,
                      Color::White,
                      "---------------------------------------------------------------------------------------");
            buffer_writer
                .lock()
                .unwrap()
                .print(&stdout)
                .expect("Could not write to stdout.");
        }

        if quiet {
            println!("0x{} {}", result.address, result.private_key);
        }

        if !matches.is_present("stream") {
            break;
        }
    }
}
