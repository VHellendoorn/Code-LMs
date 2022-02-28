use crate::query::{IntoQueryParts, Passthrough, QueryFilter, QueryGet, QueryModifier};

/// Trait used to apply modifiers to the base part of a `Query`.
pub trait QueryGetModifier<'a>: QueryGet<'a> + Sized {
    /// Applies an include modifier to the `Query`.
    fn include<I>(self, include: I) -> Include<Self, I>
    where
        I: QueryModifier<'a>,
    {
        Include::new(self, include)
    }

    /// Applies an exclude modifier to the `Query`.
    fn exclude<E>(self, exclude: E) -> IncludeExclude<Self, Passthrough, E>
    where
        E: QueryModifier<'a>,
    {
        IncludeExclude::new(self, Passthrough, exclude)
    }

    /// Applies a filter to the `Query`.
    fn filter<F>(self, filter: F) -> IncludeExcludeFilter<Self, Passthrough, Passthrough, F>
    where
        F: QueryFilter,
    {
        IncludeExcludeFilter::new(self, Passthrough, Passthrough, filter)
    }
}

impl<'a, G> QueryGetModifier<'a> for G
where
    G: QueryGet<'a>,
{
    // Empty
}

/// Wrapper that applies an include modifier to a `Query`.
pub struct Include<G, I> {
    get: G,
    include: I,
}

impl<'a, G, I> Include<G, I>
where
    G: QueryGet<'a>,
    I: QueryModifier<'a>,
{
    /// Applies an include modifier to the `Query`.
    pub fn new(get: G, include: I) -> Self {
        Self { get, include }
    }

    /// Applies an exclude modifier to the `Query`.
    pub fn exclude<E>(self, exclude: E) -> IncludeExclude<G, I, E>
    where
        E: QueryModifier<'a>,
    {
        IncludeExclude::new(self.get, self.include, exclude)
    }

    /// Applies a filter to the `Query`.
    pub fn filter<F>(self, filter: F) -> IncludeExcludeFilter<G, I, Passthrough, F>
    where
        F: QueryFilter,
    {
        IncludeExcludeFilter::new(self.get, self.include, Passthrough, filter)
    }
}

impl<'a, G, I> IntoQueryParts<'a> for Include<G, I>
where
    G: QueryGet<'a>,
    I: QueryModifier<'a>,
{
    type Get = G;
    type Include = I;
    type Exclude = Passthrough;
    type Filter = Passthrough;

    fn into_query_parts(self) -> (Self::Get, Self::Include, Self::Exclude, Self::Filter) {
        (self.get, self.include, Passthrough, Passthrough)
    }
}

/// Wrapper that applies include and exclude modifiers to a `Query`.
pub struct IncludeExclude<G, I, E> {
    get: G,
    include: I,
    exclude: E,
}

impl<'a, G, I, E> IncludeExclude<G, I, E>
where
    G: QueryGet<'a>,
    I: QueryModifier<'a>,
    E: QueryModifier<'a>,
{
    /// Applies include and exclude modifiers to the `Query`.
    pub fn new(get: G, include: I, exclude: E) -> Self {
        Self { get, include, exclude }
    }

    /// Applies a filter to the `Query`.
    pub fn filter<F>(self, filter: F) -> IncludeExcludeFilter<G, I, E, F>
    where
        F: QueryFilter,
    {
        IncludeExcludeFilter::new(self.get, self.include, self.exclude, filter)
    }
}

impl<'a, G, I, E> IntoQueryParts<'a> for IncludeExclude<G, I, E>
where
    G: QueryGet<'a>,
    I: QueryModifier<'a>,
    E: QueryModifier<'a>,
{
    type Get = G;
    type Include = I;
    type Exclude = E;
    type Filter = Passthrough;

    fn into_query_parts(self) -> (Self::Get, Self::Include, Self::Exclude, Self::Filter) {
        (self.get, self.include, self.exclude, Passthrough)
    }
}

/// Wrapper that applies include/exclude modifiers and a filter to a `Query`.
pub struct IncludeExcludeFilter<G, I, E, F> {
    get: G,
    include: I,
    exclude: E,
    filter: F,
}

impl<'a, G, I, E, F> IncludeExcludeFilter<G, I, E, F>
where
    G: QueryGet<'a>,
    I: QueryModifier<'a>,
    E: QueryModifier<'a>,
    F: QueryFilter,
{
    /// Applies include/exclude modifiers and a filter to the `Query`.
    pub fn new(get: G, include: I, exclude: E, filter: F) -> Self {
        Self { get, include, exclude, filter }
    }
}

impl<'a, G, I, E, F> IntoQueryParts<'a> for IncludeExcludeFilter<G, I, E, F>
where
    G: QueryGet<'a>,
    I: QueryModifier<'a>,
    E: QueryModifier<'a>,
    F: QueryFilter,
{
    type Get = G;
    type Include = I;
    type Exclude = E;
    type Filter = F;

    fn into_query_parts(self) -> (Self::Get, Self::Include, Self::Exclude, Self::Filter) {
        (self.get, self.include, self.exclude, self.filter)
    }
}
