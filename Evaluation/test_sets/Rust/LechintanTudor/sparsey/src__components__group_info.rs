use crate::components::{Group, QueryMask, StorageMask};
use std::marker::PhantomData;
use std::ops::Range;
use std::ptr::NonNull;

/// Holds grouping information about a `Component` type in a `World`.
#[derive(Clone, Copy)]
pub struct ComponentGroupInfo<'a> {
    group_family: NonNull<Group>,
    group_offset: u32,
    storage_mask: StorageMask,
    _phantom: PhantomData<&'a [Group]>,
}

impl<'a> ComponentGroupInfo<'a> {
    pub(crate) unsafe fn new(
        group_family: NonNull<Group>,
        group_offset: u32,
        storage_mask: StorageMask,
    ) -> Self {
        Self { group_family, group_offset, storage_mask, _phantom: PhantomData }
    }
}

/// Holds grouping information about a multiple components in a `World`.
pub struct QueryGroupInfo<'a> {
    group_family: NonNull<Group>,
    group_offset: u32,
    query_mask: QueryMask,
    _phantom: PhantomData<&'a [Group]>,
}

impl<'a> QueryGroupInfo<'a> {
    /// Creates a new `QueryGroupInfo`.
    pub fn new(info: ComponentGroupInfo<'a>) -> Self {
        Self {
            group_family: info.group_family,
            group_offset: info.group_offset,
            query_mask: QueryMask::new(info.storage_mask, 0),
            _phantom: PhantomData,
        }
    }

    /// Tries to include the given `ComponentInfo`.
    pub fn include(self, info: ComponentGroupInfo<'a>) -> Option<Self> {
        if self.group_family != info.group_family {
            return None;
        }

        Some(Self {
            group_family: self.group_family,
            group_offset: self.group_offset.max(info.group_offset),
            query_mask: self.query_mask.include(info.storage_mask),
            _phantom: PhantomData,
        })
    }

    /// Tries to exclude the given `ComponentInfo`.
    pub fn exclude(self, info: ComponentGroupInfo<'a>) -> Option<Self> {
        if self.group_family != info.group_family {
            return None;
        }

        Some(Self {
            group_family: self.group_family,
            group_offset: self.group_offset.max(info.group_offset),
            query_mask: self.query_mask.exclude(info.storage_mask),
            _phantom: PhantomData,
        })
    }

    /// Returns the range of grouped components, if any.
    pub fn group_range(self) -> Option<Range<usize>> {
        let group_family = self.group_family.as_ptr();
        let group = unsafe { *group_family.add(self.group_offset as usize) };

        if self.query_mask == group.include_mask() {
            Some(0..group.len())
        } else if self.query_mask == group.exclude_mask() {
            let prev_group = unsafe { *group_family.add((self.group_offset - 1) as usize) };
            Some(group.len()..prev_group.len())
        } else {
            None
        }
    }
}
