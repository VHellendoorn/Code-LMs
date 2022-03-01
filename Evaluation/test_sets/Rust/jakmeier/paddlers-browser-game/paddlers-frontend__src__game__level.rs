use crate::gui::{gui_components::*, utils::*};
use specs::prelude::*;

#[derive(Component, Debug, Clone, Copy)]
#[storage(HashMapStorage)]
pub struct Level {
    pub lvl: i32,
    pub exp: i32,
}
impl Level {
    pub fn menu_table_infos<'a>(&self) -> Vec<TableRow<'a>> {
        let row = TableRow::ProgressBar(
            DARK_GREEN,
            YELLOW,
            self.exp,
            self.exp_to_next_lvl(),
            Some(format!("{}", self.lvl)),
        );
        vec![row]
    }
    pub fn add_exp(&mut self, n: i32) {
        self.exp += n;
        while self.exp >= self.exp_to_next_lvl() {
            self.exp -= self.exp_to_next_lvl();
            self.lvl += 1;
        }
    }
    fn exp_to_next_lvl(&self) -> i32 {
        paddlers_shared_lib::game_mechanics::worker::hero_level_exp(self.lvl)
    }
}
