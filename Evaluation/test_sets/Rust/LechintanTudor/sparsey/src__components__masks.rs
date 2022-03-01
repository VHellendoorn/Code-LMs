pub(crate) type FamilyMask = u32;
pub(crate) type GroupMask = u32;
pub(crate) type StorageMask = u16;

pub(crate) fn new_group_mask(index: usize, arity: usize, family_arity: usize) -> GroupMask {
    ((1 << (family_arity + 1 - arity)) - 1) << index
}

pub(crate) fn iter_bit_indexes(mask: u32) -> BitIndexIter {
    BitIndexIter::new(mask)
}

#[derive(Copy, Clone, Eq, PartialEq, Default, Debug)]
pub(crate) struct QueryMask {
    include: StorageMask,
    exclude: StorageMask,
}

impl QueryMask {
    pub const fn new(include: StorageMask, exclude: StorageMask) -> Self {
        Self { include, exclude }
    }

    pub const fn new_include_group(arity: usize) -> Self {
        Self { include: (1 << arity) - 1, exclude: 0 }
    }

    pub const fn new_exclude_group(prev_arity: usize, arity: usize) -> Self {
        if prev_arity != 0 {
            let exclude_count = arity - prev_arity;

            Self {
                include: (1 << prev_arity) - 1,
                exclude: ((1 << exclude_count) - 1) << prev_arity,
            }
        } else {
            Self::new(0, 0)
        }
    }

    pub const fn include(self, include: StorageMask) -> Self {
        Self { include: self.include | include, ..self }
    }

    pub const fn exclude(self, exclude: StorageMask) -> Self {
        Self { exclude: self.exclude | exclude, ..self }
    }
}

#[derive(Clone, Debug)]
pub(crate) struct BitIndexIter {
    mask: u32,
    offset: u32,
}

impl BitIndexIter {
    fn new(mask: u32) -> BitIndexIter {
        BitIndexIter { mask, offset: 0 }
    }
}

impl Iterator for BitIndexIter {
    type Item = usize;

    fn next(&mut self) -> Option<Self::Item> {
        let trailing_zeros = self.mask.trailing_zeros();

        if trailing_zeros == u32::BITS {
            return None;
        }

        self.mask >>= trailing_zeros + 1;
        self.offset += trailing_zeros;

        let index = self.offset as usize;
        self.offset += 1;

        Some(index)
    }
}
