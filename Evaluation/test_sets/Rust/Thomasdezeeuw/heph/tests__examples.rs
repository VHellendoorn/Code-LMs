//! These tests perform an end-to-end test based on the examples in the examples
//! directory.

#![feature(stmt_expr_attributes)]

use std::io::{self, Read};
use std::net::{SocketAddr, TcpStream};
use std::ops::{Deref, DerefMut};
use std::panic;
use std::process::{Child, Command, Stdio};
use std::thread::sleep;
use std::time::Duration;

use mio_signals::{send_signal, Signal};

#[test]
fn test_1_hello_world() {
    let output = run_example_output("1_hello_world");
    assert_eq!(output, "Hello World\n");
}

#[test]
fn test_2_my_ip() {
    let mut child = run_example("2_my_ip");

    // First read the startup message, ensuring the runtime has time to start
    // up.
    let expected =
        "lvl=\"INFO\" msg=\"listening on 127.0.0.1:7890\" target=\"2_my_ip\" module=\"2_my_ip\"\n";
    let mut output = vec![0; expected.len() + 1];
    #[rustfmt::skip]
    let n = child.inner.stderr.as_mut().unwrap().read(&mut output).unwrap();
    assert_eq!(n, expected.len());
    let got = std::str::from_utf8(&output[..n]).unwrap();
    assert_eq!(expected, got);

    // Connect to the running example.
    let address = "127.0.0.1:7890".parse().unwrap();
    let mut stream = tcp_retry_connect(address);
    let mut output = String::new();
    stream
        .read_to_string(&mut output)
        .expect("unable to to read from stream");
    assert_eq!(output, "127.0.0.1");

    if let Err(err) = send_signal(child.inner.id(), Signal::Interrupt) {
        panic!("unexpected error sending signal to process: {}", err);
    }
}

#[test]
fn test_3_rpc() {
    let output = run_example_output("3_rpc");
    assert_eq!(
        output,
        "Got a RPC request: Ping\nGot a RPC response: Pong\n"
    );
}

#[test]
fn test_4_sync_actor() {
    let output = run_example_output("4_sync_actor");
    assert_eq!(output, "Got a message: Hello world\nBye\n");
}

#[test]
fn test_6_process_signals() {
    let mut child = run_example("6_process_signals");

    let want_greetings = &[
        "Got a message: Hello sync actor",
        "Got a message: Hello thread local actor",
        "Got a message: Hello thread safe actor",
    ];
    // All lines, including new line bytes + 1.
    let length = want_greetings.iter().map(|l| l.len()).sum::<usize>() + want_greetings.len();
    let mut output = vec![0; length + 1];

    // First read all greeting messages, ensuring the runtime has time to start
    // up.
    let mut n = 0;
    let mut max = want_greetings.len();
    #[rustfmt::skip]
    while n < length  {
        if max == 0 {
            panic!("too many reads, read only {}/{} bytes", n, length);
        }
        max -= 1;
        n += child.inner.stdout.as_mut().unwrap().read(&mut output[n..]).unwrap();
    }
    assert_eq!(n, length);
    output.truncate(n);
    let output = String::from_utf8(output).unwrap();

    // Because the order in which the actors are run is unspecified we don't
    // know the order of the output.
    let mut lines = output.lines();
    let mut got_greetings: Vec<&str> = (&mut lines).take(3).collect();
    got_greetings.sort_unstable();
    assert_eq!(got_greetings, want_greetings);

    // After we know the runtime started we can send it a process signal to
    // start the actual test.
    if let Err(err) = send_signal(child.inner.id(), Signal::Interrupt) {
        panic!("unexpected error sending signal to process: {}", err);
    }

    // Read the remainder of the output, expecting the shutdown messages.
    let output = read_output(child);
    let lines = output.lines();
    let mut got_shutdown: Vec<&str> = lines.collect();
    got_shutdown.sort_unstable();
    let want_shutdown = [
        "shutting down the synchronous actor",
        "shutting down the thread local actor",
        "shutting down the thread safe actor",
    ];
    assert_eq!(got_shutdown, want_shutdown);
}

#[test]
fn test_7_restart_supervisor() {
    // Index of the "?" in the string below.
    const LEFT_INDEX: usize = 51;

    let output = run_example_output("7_restart_supervisor");
    let mut lines = output.lines();

    let mut expected = "lvl=\"WARN\" msg=\"print actor failed, restarting it (?/5 restarts left): can't print message synchronously 'Hello world!': actor message 'Hello world!'\" target=\"7_restart_supervisor\" module=\"7_restart_supervisor\"".to_owned();
    for left in (0..5).rev() {
        let line = lines.next().unwrap();

        unsafe {
            expected.as_bytes_mut()[LEFT_INDEX] = b'0' + left;
        }
        assert_eq!(line, expected);
    }

    let expected = "lvl=\"WARN\" msg=\"print actor failed, stopping it (no restarts left): can't print message synchronously 'Hello world!': actor message 'Hello world!'\" target=\"7_restart_supervisor\" module=\"7_restart_supervisor\"";
    let last_line = lines.next().unwrap();
    assert_eq!(last_line, expected);

    let mut expected = "lvl=\"WARN\" msg=\"print actor failed, restarting it (?/5 restarts left): can't print message 'Hello world!': actor message 'Hello world!'\" target=\"7_restart_supervisor\" module=\"7_restart_supervisor\"".to_owned();
    for left in (0..5).rev() {
        let line = lines.next().unwrap();

        unsafe {
            expected.as_bytes_mut()[LEFT_INDEX] = b'0' + left;
        }
        assert_eq!(line, expected);
    }

    let expected = "lvl=\"WARN\" msg=\"print actor failed, stopping it (no restarts left): can't print message 'Hello world!': actor message 'Hello world!'\" target=\"7_restart_supervisor\" module=\"7_restart_supervisor\"";
    let last_line = lines.next().unwrap();
    assert_eq!(last_line, expected);

    // Expect no more output.
    assert_eq!(lines.next(), None);
}

/// Wrapper around a `command::Child` that kills the process when dropped, even
/// if the test failed. Sometimes the child command would survive the test when
/// running then in a loop (e.g. with `cargo watch`). This caused problems when
/// trying to bind to the same port again.
#[derive(Debug)]
struct ChildCommand {
    inner: Child,
}

impl Deref for ChildCommand {
    type Target = Child;

    fn deref(&self) -> &Child {
        &self.inner
    }
}

impl DerefMut for ChildCommand {
    fn deref_mut(&mut self) -> &mut Child {
        &mut self.inner
    }
}

impl Drop for ChildCommand {
    fn drop(&mut self) {
        let _ = self.inner.kill();
        self.inner.wait().expect("can't wait on child process");
    }
}

/// Run an example and return it's output.
fn run_example_output(name: &'static str) -> String {
    let child = run_example(name);
    read_output(child)
}

/// Run an already build example
fn run_example(name: &'static str) -> ChildCommand {
    let paths = [
        format!("target/debug/examples/{}", name),
        // NOTE: this is not great. These target triples should really comes
        // from rustc/cargo, but this works for now.
        #[cfg(target_os = "macos")]
        format!("target/x86_64-apple-darwin/debug/examples/{}", name),
        #[cfg(target_os = "linux")]
        format!("target/x86_64-unknown-linux-gnu/debug/examples/{}", name),
        #[cfg(target_os = "freebsd")]
        format!("target/x86_64-unknown-freebsd/debug/examples/{}", name),
    ];

    let mut errs = Vec::new();
    for path in paths.iter() {
        let res = Command::new(path)
            .stdin(Stdio::null())
            .stderr(Stdio::piped())
            .stdout(Stdio::piped())
            .spawn()
            .map(|inner| ChildCommand { inner });
        match res {
            Ok(cmd) => return cmd,
            Err(ref err) if err.kind() == io::ErrorKind::NotFound => continue,
            Err(err) => errs.push(err),
        }
    }

    panic!("failed to run example '{}': errors: {:?}", name, errs);
}

/// Read the standard output of the child command.
fn read_output(mut child: ChildCommand) -> String {
    child.wait().expect("error running example");

    let mut stdout = child.stdout.take().unwrap();
    let mut stderr = child.stderr.take().unwrap();
    let mut output = String::new();
    stdout
        .read_to_string(&mut output)
        .expect("error reading standard output of example");
    stderr
        .read_to_string(&mut output)
        .expect("error reading standard error of example");
    output
}

fn tcp_retry_connect(address: SocketAddr) -> TcpStream {
    for _ in 0..10 {
        if let Ok(stream) = TcpStream::connect(address) {
            return stream;
        }
        sleep(Duration::from_millis(10));
    }
    panic!("failed to connect to address");
}
