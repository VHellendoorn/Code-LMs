// Copyright 2021 TiKV Project Authors. Licensed under Apache-2.0.

//! A drop-in replacement for [`std::time::Instant`](https://doc.rust-lang.org/std/time/struct.Instant.html)
//! that measures time with high performance and high accuracy powered by [TSC](https://en.wikipedia.org/wiki/Time_Stamp_Counter).
//!
//! ## Example
//!
//! ```
//! let start = minstant::Instant::now();
//!
//! // Code snipppet to measure
//!
//! let duration: std::time::Duration = start.elapsed();
//! ```
//!
//! ## Platform Support
//!
//! Currently, only the Linux on `x86` or `x86_64` is backed by [TSC](https://en.wikipedia.org/wiki/Time_Stamp_Counter).
//! On other platforms, `minstant` falls back to coarse time.
//!
//! ## Calibration
//!
//! [TSC](https://en.wikipedia.org/wiki/Time_Stamp_Counter) doesn’t necessarily ticks in constant speed and even
//! doesn't synchronize across CPU cores. The calibration detects the TSC deviation and calculates the correction
//! factors with the assistance of a source wall clock. Once the deviation is beyond a crazy threshold, the calibration
//! will fail, and then we will fall back to coarse time.
//!
//! This calibration is stored globally and reused. In order to start the calibration before any call to `minstant`
//! as to make sure that the time spent on `minstant` is constant, we link the calibration into application's
//! initialization linker section, so it'll get executed once the process starts.
//!
//! *[See also the `Instant` type](crate::Instant).*

mod coarse_now;
mod instant;
#[cfg(all(target_os = "linux", any(target_arch = "x86", target_arch = "x86_64")))]
mod tsc_now;

pub use instant::{Anchor, Instant};

/// Return `true` if the current platform supports [TSC](https://en.wikipedia.org/wiki/Time_Stamp_Counter),
/// and the calibration has succeed.
///
/// The result is always the same during the lifetime of the application process.
#[inline]
pub fn is_tsc_available() -> bool {
    #[cfg(all(target_os = "linux", any(target_arch = "x86", target_arch = "x86_64")))]
    {
        tsc_now::is_tsc_available()
    }
    #[cfg(not(all(target_os = "linux", any(target_arch = "x86", target_arch = "x86_64"))))]
    {
        false
    }
}

#[inline]
pub(crate) fn current_cycle() -> u64 {
    #[cfg(all(target_os = "linux", any(target_arch = "x86", target_arch = "x86_64")))]
    if is_tsc_available() {
        tsc_now::current_cycle()
    } else {
        coarse_now::current_cycle()
    }
    #[cfg(not(all(target_os = "linux", any(target_arch = "x86", target_arch = "x86_64"))))]
    {
        coarse_now::current_cycle()
    }
}

#[inline]
pub(crate) fn nanos_per_cycle() -> f64 {
    #[cfg(all(target_os = "linux", any(target_arch = "x86", target_arch = "x86_64")))]
    {
        tsc_now::nanos_per_cycle()
    }
    #[cfg(not(all(target_os = "linux", any(target_arch = "x86", target_arch = "x86_64"))))]
    {
        1.0
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use rand::Rng;
    use std::time::{Duration, Instant as StdInstant};

    #[test]
    fn test_is_tsc_available() {
        let _ = is_tsc_available();
    }

    #[test]
    fn test_monotonic() {
        let mut prev = 0;
        for _ in 0..10000 {
            let cur = current_cycle();
            assert!(cur >= prev);
            prev = cur;
        }
    }

    #[test]
    fn test_nanos_per_cycle() {
        let _ = nanos_per_cycle();
    }

    #[test]
    fn test_duration() {
        let mut rng = rand::thread_rng();
        for _ in 0..10 {
            let instant = Instant::now();
            let std_instant = StdInstant::now();
            std::thread::sleep(Duration::from_millis(rng.gen_range(100..500)));
            let check = move || {
                let duration_ns_minstant = instant.elapsed();
                let duration_ns_std = std_instant.elapsed();

                #[cfg(target_os = "windows")]
                let expect_max_delta_ns = 20_000_000;
                #[cfg(not(target_os = "windows"))]
                let expect_max_delta_ns = 5_000_000;

                let real_delta = (duration_ns_std.as_nanos() as i128
                    - duration_ns_minstant.as_nanos() as i128)
                    .abs();
                assert!(
                    real_delta < expect_max_delta_ns,
                    "real delta: {}",
                    real_delta
                );
            };
            check();
            std::thread::spawn(check).join().expect("join failed");
        }
    }
}
