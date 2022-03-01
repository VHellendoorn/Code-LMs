//! Module with the type part of a HTTP message head.

use std::fmt;

pub mod header;
pub mod method;
mod status_code;
pub mod version;

#[doc(no_inline)]
pub use header::{Header, HeaderName, Headers};
#[doc(no_inline)]
pub use method::Method;
pub use status_code::StatusCode;
#[doc(no_inline)]
pub use version::Version;

use crate::{Request, Response};
use header::FromHeaderValue;

/// Head of a [`Request`].
pub struct RequestHead {
    method: Method,
    pub(crate) path: String,
    version: Version,
    pub(crate) headers: Headers,
}

impl RequestHead {
    /// Create a new request head.
    pub const fn new(
        method: Method,
        path: String,
        version: Version,
        headers: Headers,
    ) -> RequestHead {
        RequestHead {
            method,
            path,
            version,
            headers,
        }
    }

    /// Returns the HTTP method of this request.
    pub const fn method(&self) -> Method {
        self.method
    }

    /// Returns a mutable reference to the HTTP method of this request.
    pub const fn method_mut(&mut self) -> &mut Method {
        &mut self.method
    }

    /// Returns the path of this request.
    pub fn path(&self) -> &str {
        &self.path
    }

    /// Returns the HTTP version of this request.
    ///
    /// # Notes
    ///
    /// Requests from the [`HttpServer`] will return the highest version it
    /// understands, e.g. if a client used HTTP/1.2 (which doesn't exists) the
    /// version would be set to HTTP/1.1 (the highest version this crate
    /// understands) per RFC 7230 section 2.6.
    ///
    /// [`HttpServer`]: crate::HttpServer
    pub const fn version(&self) -> Version {
        self.version
    }

    /// Returns a mutable reference to the HTTP version of this request.
    pub const fn version_mut(&mut self) -> &mut Version {
        &mut self.version
    }

    /// Returns the headers.
    pub const fn headers(&self) -> &Headers {
        &self.headers
    }

    /// Returns mutable access to the headers.
    pub const fn headers_mut(&mut self) -> &mut Headers {
        &mut self.headers
    }

    /// Get the header’s value with `name`, if any.
    ///
    /// See [`Headers::get_value`] for more information.
    pub fn header<'a, T>(&'a self, name: &HeaderName<'_>) -> Result<Option<T>, T::Err>
    where
        T: FromHeaderValue<'a>,
    {
        self.headers.get_value(name)
    }

    /// Get the header’s value with `name` or return `default`.
    ///
    /// If no header with `name` is found or the [`FromHeaderValue`]
    /// implementation fails this will return `default`. For more control over
    /// the error handling see [`RequestHead::header`].
    pub fn header_or<'a, T>(&'a self, name: &HeaderName<'_>, default: T) -> T
    where
        T: FromHeaderValue<'a>,
    {
        match self.header(name) {
            Ok(Some(value)) => value,
            _ => default,
        }
    }

    /// Get the header’s value with `name` or returns the result of `default`.
    ///
    /// Same as [`RequestHead::header_or`] but uses a function to create the
    /// default value.
    pub fn header_or_else<'a, F, T>(&'a self, name: &HeaderName<'_>, default: F) -> T
    where
        T: FromHeaderValue<'a>,
        F: FnOnce() -> T,
    {
        match self.header(name) {
            Ok(Some(value)) => value,
            _ => default(),
        }
    }

    /// Add a body to the request head creating a complete request.
    pub const fn add_body<B>(self, body: B) -> Request<B> {
        Request::from_head(self, body)
    }
}

impl fmt::Debug for RequestHead {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("RequestHead")
            .field("method", &self.method)
            .field("path", &self.path)
            .field("version", &self.version)
            .field("headers", &self.headers)
            .finish()
    }
}

/// Head of a [`Response`].
pub struct ResponseHead {
    version: Version,
    status: StatusCode,
    pub(crate) headers: Headers,
}

impl ResponseHead {
    /// Create a new response head.
    pub const fn new(version: Version, status: StatusCode, headers: Headers) -> ResponseHead {
        ResponseHead {
            version,
            status,
            headers,
        }
    }

    /// Returns the HTTP version of this response.
    pub const fn version(&self) -> Version {
        self.version
    }

    /// Returns a mutable reference to the HTTP version of this response.
    pub const fn version_mut(&mut self) -> &mut Version {
        &mut self.version
    }

    /// Returns the response code.
    pub const fn status(&self) -> StatusCode {
        self.status
    }

    /// Returns a mutable reference to the response code.
    pub const fn status_mut(&mut self) -> &mut StatusCode {
        &mut self.status
    }

    /// Returns the headers.
    pub const fn headers(&self) -> &Headers {
        &self.headers
    }

    /// Returns mutable access to the headers.
    pub const fn headers_mut(&mut self) -> &mut Headers {
        &mut self.headers
    }

    /// Get the header’s value with `name`, if any.
    ///
    /// See [`Headers::get_value`] for more information.
    pub fn header<'a, T>(&'a self, name: &HeaderName<'_>) -> Result<Option<T>, T::Err>
    where
        T: FromHeaderValue<'a>,
    {
        self.headers.get_value(name)
    }

    /// Get the header’s value with `name` or return `default`.
    ///
    /// If no header with `name` is found or the [`FromHeaderValue`]
    /// implementation fails this will return `default`. For more control over
    /// the error handling see [`ResponseHead::header`].
    pub fn header_or<'a, T>(&'a self, name: &HeaderName<'_>, default: T) -> T
    where
        T: FromHeaderValue<'a>,
    {
        match self.header(name) {
            Ok(Some(value)) => value,
            _ => default,
        }
    }

    /// Get the header’s value with `name` or returns the result of `default`.
    ///
    /// Same as [`ResponseHead::header_or`] but uses a function to create the
    /// default value.
    pub fn header_or_else<'a, F, T>(&'a self, name: &HeaderName<'_>, default: F) -> T
    where
        T: FromHeaderValue<'a>,
        F: FnOnce() -> T,
    {
        match self.header(name) {
            Ok(Some(value)) => value,
            _ => default(),
        }
    }

    /// Add a body to the response head creating a complete response.
    pub const fn add_body<B>(self, body: B) -> Response<B> {
        Response::from_head(self, body)
    }
}

impl fmt::Debug for ResponseHead {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("ResponseHead")
            .field("version", &self.version)
            .field("status", &self.status)
            .field("headers", &self.headers)
            .finish()
    }
}
