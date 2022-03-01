use std::collections::BTreeMap;

#[no_mangle]
pub extern "C" fn insert_lookup(v: i32) -> i32 {
    let mut m: BTreeMap<String, i32> = BTreeMap::new();
    m.insert(format!("{}", v), v * 2);
    *m.get(format!("{}", v).as_str()).unwrap()
}

fn main() {
    println!("{}", insert_lookup(21));
}
