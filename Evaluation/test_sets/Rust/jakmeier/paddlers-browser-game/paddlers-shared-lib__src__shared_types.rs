//! A few basic types shared between crates.
//! Shared types related to API or other specific parts are defined in their corresponding module and not in here.

/// The default ID Type for referencing objects across the Paddlers services.
pub type PadlId = i64;

#[derive(Default, Debug, Copy, Clone, PartialEq, PartialOrd)]
/// Micro second precision
pub struct Timestamp(i64);
#[allow(dead_code)]
impl Timestamp {
    #[inline(always)]
    pub fn from_us(us: i64) -> Self {
        Timestamp(us)
    }
    #[inline(always)]
    pub fn from_millis(ms: i64) -> Self {
        Timestamp(ms * 1000)
    }
    #[inline(always)]
    pub fn from_seconds(s: i64) -> Self {
        Timestamp(s * 1_000_000)
    }
    #[inline(always)]
    pub fn from_float_seconds(s: f32) -> Self {
        Timestamp((s * 1_000_000.0) as i64)
    }
    #[inline(always)]
    pub fn micros(&self) -> i64 {
        self.0
    }
    #[inline(always)]
    pub fn millis(&self) -> i64 {
        self.0 / 1000
    }
    #[inline(always)]
    pub fn seconds(&self) -> i64 {
        self.0 / 1000_000
    }
    #[inline(always)]
    pub fn seconds_float(&self) -> f32 {
        self.0 as f32 / 1000_000.0
    }
    pub fn as_duration(&self) -> chrono::Duration {
        chrono::Duration::microseconds(self.0)
    }
}

impl std::ops::Add for Timestamp {
    type Output = Self;

    fn add(self, other: Self) -> Self {
        Self(self.0 + other.0)
    }
}

impl std::ops::Sub for Timestamp {
    type Output = Self;

    fn sub(self, other: Self) -> Self {
        Self(self.0 - other.0)
    }
}

use chrono::{Duration, NaiveDateTime};
impl std::ops::Add<Duration> for Timestamp {
    type Output = Self;

    fn add(self, other: Duration) -> Self {
        Self(self.0 + other.num_microseconds().unwrap())
    }
}
impl std::ops::Sub<Duration> for Timestamp {
    type Output = Self;

    fn sub(self, other: Duration) -> Self {
        Self(self.0 - other.num_microseconds().unwrap())
    }
}
impl Into<Duration> for Timestamp {
    fn into(self) -> Duration {
        Duration::microseconds(self.micros())
    }
}

impl From<NaiveDateTime> for Timestamp {
    fn from(other: NaiveDateTime) -> Self {
        Timestamp::from_us(other.timestamp() * 1_000_000 + other.timestamp_subsec_micros() as i64)
    }
}
