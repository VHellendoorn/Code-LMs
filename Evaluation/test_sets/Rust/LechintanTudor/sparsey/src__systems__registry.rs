use crate::components::Component;
use crate::layout::ComponentInfo;
use crate::resources::Resource;
use crate::storage::Ticks;
use crate::systems::{CommandBuffers, Commands};
use crate::world::{BorrowWorld, Comp, CompMut, Res, ResMut, World};
use std::any::TypeId;

/// Types of data that can be accessed by a `System`.
pub enum RegistryAccess {
    /// Command buffer for queueing commands.
    Commands,
    /// Shared view over a component storage.
    Comp(ComponentInfo),
    /// Exclusive view over a component storage.
    CompMut(ComponentInfo),
    /// Shared view over a resource.
    Res(TypeId),
    /// Exclusive view over a resource.
    ResMut(TypeId),
}

impl RegistryAccess {
    /// Check if two `RegistryAccess`es conflict, preventing two systems from
    /// running in parallel.
    pub fn conflicts(&self, other: &RegistryAccess) -> bool {
        match (self, other) {
            (Self::Comp(comp1), Self::CompMut(comp2)) => comp1 == comp2,
            (Self::CompMut(comp1), Self::Comp(comp2)) => comp1 == comp2,
            (Self::CompMut(comp1), Self::CompMut(comp2)) => comp1 == comp2,
            (Self::Res(res1), Self::ResMut(res2)) => res1 == res2,
            (Self::ResMut(res1), Self::Res(res2)) => res1 == res2,
            (Self::ResMut(res1), Self::ResMut(res2)) => res1 == res2,
            _ => false,
        }
    }
}

/// Execution environment for `System`s.
pub struct Registry<'a> {
    world: &'a World,
    command_buffers: &'a CommandBuffers,
    change_tick: Ticks,
}

unsafe impl Send for Registry<'_> {}
unsafe impl Sync for Registry<'_> {}

impl<'a> Registry<'a> {
    pub(crate) unsafe fn new(
        world: &'a World,
        command_buffers: &'a CommandBuffers,
        change_tick: Ticks,
    ) -> Self {
        Self { world, command_buffers, change_tick }
    }
}

/// Used by systems to borrow data from registries.
pub unsafe trait BorrowRegistry<'a> {
    /// The data resulting from the borrow.
    type Item;

    /// Returns he type of data acessed.
    fn access() -> RegistryAccess;

    /// Borrows data from the registry.
    ///
    /// # Safety
    /// The caller must ensure that !Send and !Sync items are borrowed
    /// correctly.
    unsafe fn borrow(registry: &'a Registry) -> Self::Item;
}

unsafe impl<'a, 'b> BorrowRegistry<'a> for Commands<'b> {
    type Item = Commands<'a>;

    fn access() -> RegistryAccess {
        RegistryAccess::Commands
    }

    unsafe fn borrow(registry: &'a Registry) -> Self::Item {
        Commands::new(registry.command_buffers.next().unwrap(), registry.world.entity_storage())
    }
}

unsafe impl<'a, 'b, T> BorrowRegistry<'a> for Comp<'b, T>
where
    T: Component,
{
    type Item = Comp<'a, T>;

    fn access() -> RegistryAccess {
        RegistryAccess::Comp(ComponentInfo::new::<T>())
    }

    unsafe fn borrow(registry: &'a Registry) -> Self::Item {
        <Self as BorrowWorld>::borrow(registry.world, registry.change_tick)
    }
}

unsafe impl<'a, 'b, T> BorrowRegistry<'a> for CompMut<'b, T>
where
    T: Component,
{
    type Item = CompMut<'a, T>;

    fn access() -> RegistryAccess {
        RegistryAccess::CompMut(ComponentInfo::new::<T>())
    }

    unsafe fn borrow(registry: &'a Registry) -> Self::Item {
        <Self as BorrowWorld>::borrow(registry.world, registry.change_tick)
    }
}

unsafe impl<'a, 'b, T> BorrowRegistry<'a> for Res<'b, T>
where
    T: Resource,
{
    type Item = Res<'a, T>;

    fn access() -> RegistryAccess {
        RegistryAccess::Res(TypeId::of::<T>())
    }

    unsafe fn borrow(registry: &'a Registry) -> Self::Item {
        <Self as BorrowWorld>::borrow(registry.world, registry.change_tick)
    }
}

unsafe impl<'a, 'b, T> BorrowRegistry<'a> for ResMut<'b, T>
where
    T: Resource,
{
    type Item = ResMut<'a, T>;

    fn access() -> RegistryAccess {
        RegistryAccess::ResMut(TypeId::of::<T>())
    }

    unsafe fn borrow(registry: &'a Registry) -> Self::Item {
        <Self as BorrowWorld>::borrow(registry.world, registry.change_tick)
    }
}

unsafe impl<'a, 'b, T> BorrowRegistry<'a> for Option<Res<'b, T>>
where
    T: Resource,
{
    type Item = Option<Res<'a, T>>;

    fn access() -> RegistryAccess {
        RegistryAccess::Res(TypeId::of::<T>())
    }

    unsafe fn borrow(registry: &'a Registry) -> Self::Item {
        <Self as BorrowWorld>::borrow(registry.world, registry.change_tick)
    }
}

unsafe impl<'a, 'b, T> BorrowRegistry<'a> for Option<ResMut<'b, T>>
where
    T: Resource,
{
    type Item = Option<ResMut<'a, T>>;

    fn access() -> RegistryAccess {
        RegistryAccess::ResMut(TypeId::of::<T>())
    }

    unsafe fn borrow(registry: &'a Registry) -> Self::Item {
        <Self as BorrowWorld>::borrow(registry.world, registry.change_tick)
    }
}
