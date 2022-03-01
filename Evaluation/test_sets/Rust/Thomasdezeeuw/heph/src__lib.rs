//! Heph, derived from [Hephaestus], is the Greek god of blacksmiths,
//! metalworking, carpenters, craftsmen, artisans, sculptors, metallurgy, fire,
//! and volcanoes. Well this crate has very little to do with Greek gods, but I
//! needed a name.
//!
//! [Hephaestus]: https://en.wikipedia.org/wiki/Hephaestus
//!
//! ## About
//!
//! Heph is an [actor] framework based on asynchronous functions. Such an
//! asynchronous function looks like this:
//!
//! ```
//! # use heph::actor;
//! # use heph::rt::ThreadLocal;
//! #
//! async fn actor(mut ctx: actor::Context<String, ThreadLocal>) {
//!     // Receive a message.
//!     if let Ok(msg) = ctx.receive_next().await {
//!         // Print the message.
//!         println!("got a message: {}", msg);
//!     }
//! }
//! #
//! # drop(actor); // Silence dead code warnings.
//! ```
//!
//! Heph uses an event-driven, non-blocking I/O, share nothing design. But what
//! do all those buzzwords actually mean?
//!
//!  - *Event-driven*: Heph does nothing by itself, it must first get an event
//!    before it starts doing anything. For example when using a [`TcpListener`]
//!    it waits on a notification from the OS saying the `TcpListener` is ready
//!    before trying to accept connections.
//!  - *Non-blocking I/O*: normal I/O operations need to wait (block) until the
//!    operation can complete. Using non-blocking, or asynchronous, I/O means
//!    that rather then waiting for the operation to complete we'll do some
//!    other, more useful, work and try the operation later.
//!  - *Share nothing*: a lot of application share data across multiple threads.
//!    To do this safely we need to protect it from data races, via a [`Mutex`]
//!    or by using [atomic] operations. Heph is designed to not share any data.
//!    Each actor is responsible for its own memory and cannot access memory
//!    owned by other actors. Instead communication is done via sending
//!    messages, see the [actor model].
//!
//! [actor]: https://en.wikipedia.org/wiki/Actor_model
//! [`TcpListener`]: crate::net::TcpListener
//! [`Mutex`]: std::sync::Mutex
//! [atomic]: std::sync::atomic
//! [actor model]: https://en.wikipedia.org/wiki/Actor_model
//!
//! ## Getting started
//!
//! There are two ways to get starting with Heph. If you like to see examples,
//! take a look at the [examples] in the examples directory of the source code.
//! If you like to learn more about some of the core concepts of Heph start with
//! the [Quick Start] guide.
//!
//! [examples]: https://github.com/Thomasdezeeuw/heph/blob/master/examples/README.md
//! [Quick Start]: crate::quick_start
//!
//! ## Features
//!
//! This crate has a single optional feature: `test`. This feature will enable
//! the `test` module which adds testing facilities.

#![feature(
    arc_new_cyclic,
    array_methods,
    async_stream,
    available_parallelism,
    binary_heap_retain,
    const_fn_trait_bound,
    const_option,
    destructuring_assignment,
    doc_cfg,
    doc_cfg_hide,
    drain_filter,
    generic_associated_types,
    io_slice_advance,
    is_sorted,
    maybe_uninit_array_assume_init,
    maybe_uninit_extra,
    maybe_uninit_slice,
    maybe_uninit_uninit_array,
    never_type,
    new_uninit,
    result_into_ok_or_err,
    stmt_expr_attributes,
    vec_spare_capacity
)]
#![cfg_attr(any(test, feature = "test"), feature(once_cell))]
#![warn(
    anonymous_parameters,
    bare_trait_objects,
    missing_debug_implementations,
    missing_docs,
    rust_2018_idioms,
    trivial_numeric_casts,
    unused_extern_crates,
    unused_import_braces,
    unused_qualifications,
    unused_results,
    variant_size_differences
)]
// Disallow warnings when running tests.
#![cfg_attr(test, deny(warnings))]
// Disallow warnings in examples, we want to set a good example after all.
#![doc(test(attr(deny(warnings))))]
// The `cfg(any(test, feature = "test"))` attribute creates a doc element
// staying that it's only supporting "using test or test", that is a bit
// confusing. So we hide those parts and instead manually replace all of them
// with: `doc(cfg(feature = "test"))`. That will stay it's only supported using
// the test feature.
#![doc(cfg_hide(any(test, feature = "test")))]

#[cfg(not(any(target_os = "linux", target_os = "freebsd", target_os = "macos")))]
compile_error!("Heph currently only supports Linux, FreeBSD and macOS.");
#[cfg(not(target_pointer_width = "64"))]
compile_error!("Heph currently only supports 64 bit architectures.");

/// A macro to try an I/O function.
///
/// Note that this is used in the net and pipe modules and has to be defined
/// before use.
macro_rules! try_io {
    ($op: expr) => {
        loop {
            match $op {
                Ok(ok) => break Poll::Ready(Ok(ok)),
                Err(ref err) if err.kind() == io::ErrorKind::WouldBlock => break Poll::Pending,
                Err(ref err) if err.kind() == io::ErrorKind::Interrupted => continue,
                Err(err) => break Poll::Ready(Err(err)),
            }
        }
    };
}

pub mod actor;
pub mod actor_ref;
pub mod bytes;
pub mod log;
pub mod net;
pub mod pipe;
pub mod quick_start;
pub mod rt;
pub mod spawn;
pub mod supervisor;
#[cfg(target_os = "linux")]
pub mod systemd;
#[cfg(any(test, feature = "test"))]
#[doc(cfg(feature = "test"))]
pub mod test;
pub mod timer;
pub mod trace;
#[doc(hidden)]
pub mod util;

#[doc(no_inline)]
pub use actor::{Actor, NewActor};
#[doc(no_inline)]
pub use actor_ref::ActorRef;
#[doc(no_inline)]
pub use rt::{Runtime, RuntimeRef};
#[doc(no_inline)]
pub use spawn::{ActorOptions, Spawn, SyncActorOptions};
#[doc(no_inline)]
pub use supervisor::{Supervisor, SupervisorStrategy};
