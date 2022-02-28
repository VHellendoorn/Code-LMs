use crate::horust::signal_safe::panic_ssafe;
use nix::sys::signal::{sigaction, SaFlags, SigAction, SigHandler, SigSet, SIGINT, SIGTERM};

static mut SIGTERM_RECEIVED: bool = false;

pub(crate) fn is_sigterm_received() -> bool {
    unsafe { SIGTERM_RECEIVED }
}

pub(crate) fn clear_sigtem() {
    unsafe {
        SIGTERM_RECEIVED = false;
    }
}

/// Setup the signal handlers
pub(crate) fn init() {
    // To allow auto restart on some syscalls,
    // for example: `waitpid`.
    let flags = SaFlags::SA_RESTART;
    let sig_action = SigAction::new(SigHandler::Handler(handle_sigterm), flags, SigSet::empty());

    if let Err(err) = unsafe { sigaction(SIGTERM, &sig_action) } {
        let error = format!("sigaction() failed: {}", err);
        panic_ssafe(error.as_str(), 103);
    };

    if let Err(err) = unsafe { sigaction(SIGINT, &sig_action) } {
        let error = format!("sigaction() failed: {}", err);
        panic_ssafe(error.as_str(), 104);
    };
}

extern "C" fn handle_sigterm(_signal: libc::c_int) {
    unsafe {
        SIGTERM_RECEIVED = true;
    }
}
