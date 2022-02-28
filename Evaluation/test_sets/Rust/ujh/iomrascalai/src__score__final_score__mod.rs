/************************************************************************
 *                                                                      *
 * Copyright 2015 Urban Hafner                                          *
 *                                                                      *
 * This file is part of Iomrascálaí.                                    *
 *                                                                      *
 * Iomrascálaí is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or    *
 * (at your option) any later version.                                  *
 *                                                                      *
 * Iomrascálaí is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of       *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
 * GNU General Public License for more details.                         *
 *                                                                      *
 * You should have received a copy of the GNU General Public License    *
 * along with Iomrascálaí.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                      *
 ************************************************************************/

use board::Board;
use board::Coord;
use board::Empty;
use config::Config;
use game::Game;
use ownership::OwnershipStatistics;
use ruleset::KgsChinese;

use std::sync::Arc;

mod test;

pub struct FinalScore {
    board: Board,
    dead: Vec<Coord>,
}

impl FinalScore {

    pub fn new(config: Arc<Config>, game: &Game, ownership: &OwnershipStatistics) -> FinalScore {
        let mut board = game.board();
        let dead = Coord::for_board_size(board.size()).iter()
            .filter(|c| board.color(c) != Empty)
            .filter(|c| ownership.owner(c) != Empty)
            .filter(|c| ownership.owner(c) != board.color(c))
            .cloned()
            .collect();
        if config.ruleset == KgsChinese {
            for coord in &dead {
                board.remove_dead_stone(coord);
            }
            FinalScore {
                board: board,
                dead: dead,
            }
        } else {
            FinalScore {
                board: board,
                dead: vec!(),
            }
        }
    }

    pub fn score(&self) -> String {
        format!("{}", self.board.score())
    }

    pub fn status_list(&self, kind: &str) -> Result<String, String> {
        match kind {
            "alive" => self.status_list_alive(),
            "dead" => self.status_list_dead(),
            "seki" => self.status_list_seki(),
            _ => Err("unknown argument".to_string()),
        }
    }

    pub fn dead_stones_on_board(&self) -> bool {
        self.dead.len() > 0
    }

    fn status_list_dead(&self) -> Result<String, String> {
        let s = self.dead.iter()
            .fold(String::new(), |acc, el| format!("{} {}", acc, el.to_gtp()));

        Ok(String::from(s.trim()))
    }

    fn status_list_seki(&self) -> Result<String, String> {
        Ok("".to_string())
    }

    fn status_list_alive(&self) -> Result<String, String> {
        let s = Coord::for_board_size(self.board.size()).iter()
            .filter(|c| self.board.color(c) != Empty)
            .fold(String::new(), |acc, el| format!("{} {}", acc, el.to_gtp()));
        Ok(String::from(s.trim()))
    }

}
