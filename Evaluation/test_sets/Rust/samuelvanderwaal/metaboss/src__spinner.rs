use indicatif::{ProgressBar, ProgressStyle};

pub fn create_spinner(msg: &'static str) -> ProgressBar {
    let spinner = ProgressBar::new_spinner();
    spinner.enable_steady_tick(10);
    spinner.set_style(
        ProgressStyle::default_spinner()
            .tick_strings(&["▹▹▹▹▹", "▸▹▹▹▹", "▹▸▹▹▹", "▹▹▸▹▹", "▹▹▹▸▹", "▹▹▹▹▸", ""])
            .template("{spinner:.blue}{msg}"),
    );
    spinner.set_message(msg);
    spinner
}

pub fn create_alt_spinner(msg: &'static str) -> ProgressBar {
    let spinner = ProgressBar::new_spinner();
    spinner.enable_steady_tick(80);
    spinner.set_style(
        ProgressStyle::default_spinner()
            .tick_strings(&[
                "[    ]", "[=   ]", "[==  ]", "[=== ]", "[ ===]", "[  ==]", "[   =]", "[    ]",
                "[   =]", "[  ==]", "[ ===]", "[====]", "[=== ]", "[==  ]", "[=   ]",
            ])
            .template("{spinner:.blue} {msg}"),
    );
    spinner.set_message(msg);
    spinner
}
