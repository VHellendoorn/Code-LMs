macro_rules! cprintln {
    ($surpress:expr, $stdout:expr, $fg:expr, $($rest:tt)+) => {
        if !$surpress {
            use std::io::Write;
            use termcolor::{ColorSpec, WriteColor};

            $stdout.set_color(ColorSpec::new().set_fg(Some($fg)))
                .expect("Could not set the text formatting.");
            writeln!($stdout, $($rest)+).expect("Could not output text.");
        }
    }
}

macro_rules! cprint {
    ($surpress:expr, $stdout:expr, $fg:expr, $($rest:tt)+) => {
        if !$surpress {
            use std::io::Write;
            use termcolor::{ColorSpec, WriteColor};

            $stdout.set_color(ColorSpec::new().set_fg(Some($fg)))
                .expect("Could not set the text formatting.");
            write!($stdout, $($rest)+).expect("Could not output text.");
        }
    }
}
