// Twang
// Copyright © 2018-2021 Jeron Aldaron Lau.
//
// Licensed under any of:
// - Apache License, Version 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
// - MIT License (https://mit-license.org/)
// - Boost Software License, Version 1.0 (https://www.boost.org/LICENSE_1_0.txt)
// At your choosing (See accompanying files LICENSE_APACHE_2_0.txt,
// LICENSE_MIT.txt and LICENSE_BOOST_1_0.txt).

use crate::sig::Signal;
use fon::{mono::Mono64, Stream};
use std::{borrow::Borrow, fmt::Debug, time::Duration};

/// Frequency counter.
#[derive(Copy, Clone, Debug)]
pub struct Fc(Duration);

impl Fc {
    /// Sample frequency counter with a frequency.
    #[inline(always)]
    pub fn freq(&self, freq: f64) -> Signal {
        let modu = Duration::new(1, 0).div_f64(freq).as_nanos();
        let nano = self.0.as_nanos();
        // Return signal between -1 and 1
        (((nano % modu) << 1) as f64 / modu as f64 - 1.0).into()
    }
}

/// A streaming synthesizer.  Implements [`Stream`](fon::Stream).
pub struct Synth<T: Debug> {
    params: T,
    synthfn: fn(&mut T, Fc) -> Signal,
    counter: Duration,
    sample_rate: Option<f64>,
    stepper: Duration,
}

impl<T: Debug> Debug for Synth<T> {
    fn fmt(&self, _: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        todo!()
    }
}

impl<T: Debug> Synth<T> {
    /// Create a new streaming synthesizer.
    #[inline(always)]
    pub fn new(params: T, synth: fn(&mut T, Fc) -> Signal) -> Self {
        Self {
            params,
            sample_rate: None,
            synthfn: synth,
            counter: Duration::default(),
            stepper: Duration::default(),
        }
    }

    /// Get the parameters of the synthesizer.
    pub fn params(&mut self) -> &mut T {
        &mut self.params
    }
}

impl<T: Debug> Iterator for &mut Synth<T> {
    type Item = Mono64;

    fn next(&mut self) -> Option<Self::Item> {
        let frame =
            (self.synthfn)(&mut self.params, Fc(self.counter)).to_mono();
        self.counter += self.stepper;
        Some(frame)
    }
}

impl<T: Debug> Stream<Mono64> for &mut Synth<T> {
    fn sample_rate(&self) -> Option<f64> {
        self.sample_rate
    }

    fn set_sample_rate<R: Into<f64>>(&mut self, sr: R) {
        let sample_rate = sr.into();
        self.sample_rate = Some(sample_rate);
        self.stepper = Duration::new(1, 0).div_f64(sample_rate);
    }

    fn len(&self) -> Option<usize> {
        None
    }
}

/// Trait for synthesizing multiple sounds together.
///
/// This works on arrays, slices, and iterators over either `Signal` or
/// `&Signal`.
pub trait Mix {
    /// Add multiple signals together.
    fn mix(self) -> Signal;
}

impl<B: Borrow<Signal>, I: IntoIterator<Item = B>> Mix for I {
    #[inline(always)]
    fn mix(self) -> Signal {
        self.into_iter()
            .map(|a| f64::from(*a.borrow()))
            .sum::<f64>()
            .into()
    }
}
