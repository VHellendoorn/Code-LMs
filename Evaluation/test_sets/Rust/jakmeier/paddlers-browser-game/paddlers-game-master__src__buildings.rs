use paddlers_shared_lib::{game_mechanics::attributes::*, models::*, prelude::VillageKey};

pub struct BuildingFactory;

impl BuildingFactory {
    pub fn new(typ: BuildingType, pos: (usize, usize), village: VillageKey) -> NewBuilding {
        let now = chrono::Utc::now().naive_utc();
        NewBuilding {
            x: pos.0 as i32,
            y: pos.1 as i32,
            building_type: typ.into(),
            building_range: typ.range(),
            attack_power: typ.attack_power().map(|i| i as i32),
            attacks_per_cycle: typ.attacks_per_cycle().map(|i| i as i32),
            creation: now,
            village_id: village.num(),
            lv: 1,
        }
    }
}
