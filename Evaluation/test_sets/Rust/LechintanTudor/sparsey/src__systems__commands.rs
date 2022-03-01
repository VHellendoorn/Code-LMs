use crate::components::ComponentSet;
use crate::storage::{ChangeTicks, Entity, EntityStorage};
use crate::systems::CommandBuffer;
use crate::world::World;

/// Command buffer used for queueing commands that require exclusive access to
/// the `World`.
pub struct Commands<'a> {
    buffer: &'a mut CommandBuffer,
    entities: &'a EntityStorage,
}

impl<'a> Commands<'a> {
    pub(crate) fn new(buffer: &'a mut CommandBuffer, entities: &'a EntityStorage) -> Self {
        Self { buffer, entities }
    }

    /// Adds a command with exclusive access to the `World` to the command
    /// queue.
    pub fn queue<F>(&mut self, command: F)
    where
        F: FnOnce(&mut World) + Send + 'static,
    {
        self.buffer.push(Box::new(command));
    }

    /// Queues the creation of an entity with given `components` and returns a
    /// handle to it.
    pub fn create_entity<C>(&mut self, components: C) -> Entity
    where
        C: ComponentSet,
    {
        let entity = self.entities.create_atomic();

        self.queue(move |world| {
            let _ = world.insert_components(entity, components);
        });

        entity
    }

    /// Same as `create_entity`, but the `ChangeTicks` are provided by the
    /// caller.
    pub fn create_entity_with_ticks<C>(&mut self, components: C, ticks: ChangeTicks) -> Entity
    where
        C: ComponentSet,
    {
        let entity = self.entities.create_atomic();

        self.queue(move |world| {
            let _ = world.insert_components_with_ticks(entity, components, ticks);
        });

        entity
    }

    /// Queues the creation of entities with components yielded by
    /// `components_iter`.
    pub fn create_entities<C, I>(&mut self, components_iter: I)
    where
        C: ComponentSet,
        I: IntoIterator<Item = C> + Send + 'static,
    {
        self.queue(move |world| {
            world.create_entities(components_iter);
        });
    }

    /// Same as `create_entities`, but the `ChangeTicks` are provided by the
    /// caller.
    pub fn create_entities_with_ticks<C, I>(&mut self, components_iter: I, ticks: ChangeTicks)
    where
        C: ComponentSet,
        I: IntoIterator<Item = C> + Send + 'static,
    {
        self.queue(move |world| {
            world.create_entities_with_ticks(components_iter, ticks);
        });
    }

    /// Queues the destruction of `entity`.
    pub fn destroy_entity(&mut self, entity: Entity) {
        self.queue(move |world| {
            world.destroy_entity(entity);
        });
    }

    /// Queues the appending of `components` to `entity`.
    pub fn insert_components<C>(&mut self, entity: Entity, components: C)
    where
        C: ComponentSet,
    {
        self.queue(move |world| {
            let _ = world.insert_components(entity, components);
        });
    }

    /// Same as `insert_components`, but the `ChangeTicks` are provided by the
    /// caller.
    pub fn insert_components_with_ticks<C>(
        &mut self,
        entity: Entity,
        components: C,
        ticks: ChangeTicks,
    ) where
        C: ComponentSet,
    {
        self.queue(move |world| {
            let _ = world.insert_components_with_ticks(entity, components, ticks);
        });
    }

    /// Queues the deletion of a set of components from `entity`.
    pub fn delete_components<C>(&mut self, entity: Entity)
    where
        C: ComponentSet,
    {
        self.queue(move |world| {
            world.delete_components::<C>(entity);
        });
    }

    /// Returns a slice containing all entities in the `World`.
    pub fn entities(&self) -> &[Entity] {
        self.entities.as_ref()
    }
}
