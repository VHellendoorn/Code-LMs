/************************************************************************
 *                                                                      *
 * Copyright 2015 Urban Hafner, Igor Polyakov                           *
 * Copyright 2016 Urban Hafner                                          *
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

pub use self::controller::EngineController;
pub use self::node::Node;
use board::Color;
use board::Move;
use board::NoMove;
use board::Pass;
use board::Resign;
use board::White;
use config::Config;
use game::Game;
use ownership::OwnershipStatistics;
use patterns::SmallPatternMatcher;
use playout::Playout;
use ruleset::KgsChinese;
use score::FinalScore;
use self::worker::Answer;
use self::worker::DirectMessage;
use self::worker::Message;
use self::worker::Path;
use self::worker::Response;
use self::worker::Worker;
use timer::Timer;

use std::sync::Arc;
use std::sync::mpsc::Receiver;
use std::sync::mpsc::Sender;
use std::sync::mpsc::channel;
use std::thread::spawn;
use time::PreciseTime;

macro_rules! check {
    ($config:expr, $r:expr) => {
        check!($config, _unused_result = $r => {})
    };
    ($config:expr, $res:pat = $r:expr => $body:expr) => {
        match $r {
            Ok(res) => {
                let $res = res;
                $body
            },
            Err(e) => {
                $config.log(format!("[DEBUG] unwrap failed with {:?} at {}:{}", e, file!(), line!()));
            }
        }
    };

}

mod controller;
mod node;
mod prior;
mod worker;

pub struct Engine {
    config: Arc<Config>,
    direct_message_senders: Vec<Sender<DirectMessage>>,
    id: usize,
    ownership: OwnershipStatistics,
    playout: Arc<Playout>,
    previous_node_count: usize,
    receive_from_threads: Receiver<Response>,
    root: Node,
    send_to_main: Sender<Response>,
    small_pattern_matcher: Arc<SmallPatternMatcher>,
    start: PreciseTime,
}

impl Engine {

    pub fn new(config: Arc<Config>, small_pattern_matcher: Arc<SmallPatternMatcher>) -> Engine {
        let (send_to_main, receive_from_threads) = channel();
        let mut engine = Engine {
            config: config.clone(),
            direct_message_senders: vec!(),
            id: 0,
            ownership: OwnershipStatistics::new(config.clone(), 0, 0.0),
            playout: Arc::new(Playout::new(
                config.clone(),
                small_pattern_matcher.clone()
            )),
            previous_node_count: 0,
            receive_from_threads: receive_from_threads,
            root: Node::new(NoMove, config),
            send_to_main: send_to_main,
            small_pattern_matcher: small_pattern_matcher.clone(),
            start: PreciseTime::now(),
        };
        engine.spin_up();
        engine
    }

    pub fn ownership(&self) -> &OwnershipStatistics {
        &self.ownership
    }

    pub fn genmove(&mut self, color: Color, game: &Game, timer: &Timer) -> (Move,usize) {
        self.generic_genmove(color, game, timer, false)
    }

    pub fn genmove_cleanup(&mut self, color: Color, game: &Game, timer: &Timer) -> (Move,usize) {
        self.generic_genmove(color, game, timer, true)
    }

    fn generic_genmove(&mut self, color: Color, game: &Game, timer: &Timer, cleanup: bool) -> (Move,usize) {
        self.genmove_setup(color, game);
        if self.root.has_no_children() {
            self.config.log(format!("No moves to simulate!"));
            return (Pass(color), self.root.playouts());
        }
        let stop = |win_ratio, _| { timer.ran_out_of_time(win_ratio) };
        self.search(game, stop);
        let msg = format!("{} simulations ({}% wins on average)", self.root.playouts(), self.root.win_ratio()*100.0);
        self.config.log(msg);
        let playouts = self.root.playouts();
        let m = self.best_move(game, color, cleanup);
        self.set_new_root(&game.play(m).unwrap(), color);
        (m,playouts)
    }

    fn search<F>(&mut self, game: &Game, stop: F) where F: Fn(f32, usize) -> bool {
        self.send_new_state_to_workers(game);
        loop {
            let win_ratio = {
                let (best, _) = self.root.best();
                best.win_ratio()
            };
            let done = {
                stop(win_ratio, self.root.playouts())
            };
            if done { return; }
            let r = self.receive_from_threads.recv();
            check!(self.config, res = r => {
                self.handle_response(res, &game);
            });
        }
    }

    pub fn donplayouts(&mut self, game: &Game, playouts: usize) {
        self.ownership = OwnershipStatistics::new(self.config.clone(), game.size(), game.komi());
        if self.root.has_no_children() {
            let color = match game.last_move() {
                NoMove => White,
                _ => *game.last_move().color()
            };
            self.root = Node::root(game, color, self.config.clone());
        }
        let initial_playouts = self.root.playouts();
        let stop = |_, current_playouts: usize| {
            (current_playouts - initial_playouts) > playouts
        };
        self.search(game, stop);
    }

    fn dead_stones_on_board(&self, game: &Game) -> bool {
        FinalScore::new(self.config.clone(), game, self.ownership()).dead_stones_on_board()
    }

    fn handle_response(&mut self, response: Response, game: &Game) {
        let (answer, id, send_to_thread) = response;
        // Ignore responses from the previous genmove
        if self.id == id {
            let message = match answer {
                Answer::NewState => {
                    self.expand(game, Path::new())
                },
                Answer::RunPlayout {path, playout_result} => {
                    self.ownership.merge(playout_result.score());
                    self.root.record_on_path(path.path(), &playout_result);
                    self.expand(game, path)
                },
                Answer::CalculatePriors {path, priors} => {
                    self.root.record_priors(path.path(), priors);
                    Message::RunPlayout { path: path }
                }
            };
            check!(self.config, send_to_thread.send(message));
        }
    }

    fn expand(&mut self, game: &Game, path: Path) -> Message {
        let (path, child_moves) = self.root.find_leaf_and_expand(game, path);
        let nodes_added = child_moves.len();
        if nodes_added > 0 {
            Message::CalculatePriors {
                child_moves,
                path,
            }
        } else {
            Message::RunPlayout {
                path
            }
        }
    }

    pub fn reset(&mut self, size: u8, komi: f32) {
        self.previous_node_count = 0;
        self.root = Node::new(NoMove, self.config.clone());
        self.ownership = OwnershipStatistics::new(self.config.clone(), size, komi);
    }

    fn set_new_root(&mut self, game: &Game, color: Color) {
        self.root = self.root.find_new_root(game, color);
    }

    fn genmove_setup(&mut self, color: Color, game: &Game) {
        self.start = PreciseTime::now();
        self.config.gfx(self.ownership.gfx());
        self.ownership = OwnershipStatistics::new(self.config.clone(), game.size(), game.komi());
        self.set_new_root(game, color);
    }

    fn best_move(&self, game: &Game, color: Color, cleanup: bool) -> Move {
        let (best_node, pass) = self.root.best();
        let best_win_ratio = best_node.win_ratio();
        let pass_win_ratio = pass.win_ratio();
        let allow_pass = match game.ruleset() {
            KgsChinese => {
                // If cleanup is true it means this code was called by kgs-genmove_cleanup so we can
                // only pass if there are no dead stones on the board.
                if cleanup {
                    !self.dead_stones_on_board(game)
                } else {
                    true
                }
            },
            _ => {
                // Only allow passing under Tromp/Taylor and CGOS when we are winning.
                game.winner() == color
            }
        };
        let n = if allow_pass {
            if best_win_ratio > pass_win_ratio { best_node } else { pass }
        } else {
            best_node
        };
        let win_ratio = n.win_ratio();
        let msg = format!("Best move win ratio: {}%", win_ratio*100.0);
        self.config.log(msg);
        // Special case, when we are winning and all moves are played.
        if win_ratio == 0.0 {
            Pass(color)
        } else if win_ratio < self.config.scoring.resignation_percentage {
            Resign(color)
        } else {
            n.m()
        }
    }

    fn spin_down(&mut self) {
        for direct_message_sender in &self.direct_message_senders {
            check!(self.config, direct_message_sender.send(DirectMessage::SpinDown));
        }
        self.direct_message_senders = vec!();
    }

    fn spin_up(&mut self) {
        self.direct_message_senders = vec!();
        for _ in 0..self.config.threads {
            self.spin_up_worker();
        }
    }

    fn send_new_state_to_workers(&mut self, game: &Game) {
        self.id += 1;
        for direct_message_sender in &self.direct_message_senders {
            let dm = DirectMessage::NewState {
                board: game.board(),
                id: self.id,
            };
            check!(self.config, direct_message_sender.send(dm));
        }
    }

    fn spin_up_worker(&mut self) {
        let mut worker = Worker::new(
            &self.config,
            &self.playout,
            &self.small_pattern_matcher,
            &self.send_to_main
        );
        let (send_direct_message, receive_direct_message) = channel();
        self.direct_message_senders.push(send_direct_message);
        spawn(move || worker.run(receive_direct_message));
    }

}

impl Drop for Engine {

    fn drop(&mut self) {
        self.spin_down();
    }
}
