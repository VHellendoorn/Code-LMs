mod common;

use common::*;
use sparsey::prelude::*;
use std::any::TypeId;

#[test]
fn test_entities_crud() {
    let mut world = World::default();

    // Create
    let e0 = world.create_entity(());
    assert!(world.contains_entity(e0));
    assert_eq!(world.entities(), &[e0]);

    // Destroy
    assert!(world.destroy_entity(e0));
    assert!(!world.destroy_entity(e0));
    assert!(!world.contains_entity(e0));
    assert_eq!(world.entities(), &[]);

    // Clear
    let e0 = world.create_entity(());
    let e1 = world.create_entity(());
    world.clear_entities();
    assert!(!world.contains_entity(e0));
    assert!(!world.contains_entity(e1));
    assert_eq!(world.entities(), &[]);
}

#[test]
fn test_register() {
    let mut world = World::default();

    assert!(!world.is_registered(&TypeId::of::<A>()));
    assert!(!world.is_registered(&TypeId::of::<B>()));

    world.register::<A>();
    world.register::<B>();

    assert!(world.is_registered(&TypeId::of::<A>()));
    assert!(world.is_registered(&TypeId::of::<B>()));
}

#[test]
fn test_components_crud() {
    let mut world = World::default();
    world.register::<A>();
    world.register::<B>();
    world.register::<C>();

    // Insert
    let e0 = world.create_entity((A(0), B(0)));

    {
        let (a, b) = world.borrow::<(Comp<A>, Comp<B>)>();
        assert_eq!(a.get(e0).copied(), Some(A(0)));
        assert_eq!(b.get(e0).copied(), Some(B(0)));
    }

    // Append
    assert!(world.insert_components(e0, (C(0),)).is_ok());

    {
        let (a, b, c) = world.borrow::<(Comp<A>, Comp<B>, Comp<C>)>();
        assert_eq!(a.get(e0).copied(), Some(A(0)));
        assert_eq!(b.get(e0).copied(), Some(B(0)));
        assert_eq!(c.get(e0).copied(), Some(C(0)));
    }

    // Remove
    assert_eq!(world.remove_components::<(A, B)>(e0), Some((A(0), B(0))));
    assert_eq!(world.remove_components::<(A, B)>(e0), None);

    {
        let (a, b, c) = world.borrow::<(Comp<A>, Comp<B>, Comp<C>)>();
        assert_eq!(a.get(e0), None);
        assert_eq!(b.get(e0), None);
        assert_eq!(c.get(e0).copied(), Some(C(0)));
    }

    // Delete
    world.delete_components::<(C,)>(e0);

    {
        let c = world.borrow::<Comp<C>>();
        assert_eq!(c.get(e0), None);
    }
}
