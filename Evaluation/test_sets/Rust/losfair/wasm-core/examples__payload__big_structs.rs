pub struct Foo {
    a: i32,
    b: i64,
    c: u8,
    d: i64
}

impl Foo {
    pub fn new() -> Foo {
        Foo {
            a: 1,
            b: 2,
            c: 3,
            d: 4
        }
    }
}

#[no_mangle]
pub extern "C" fn new_foo() -> *mut Foo {
    let foo = Box::new(Foo::new());
    Box::into_raw(foo)
}
