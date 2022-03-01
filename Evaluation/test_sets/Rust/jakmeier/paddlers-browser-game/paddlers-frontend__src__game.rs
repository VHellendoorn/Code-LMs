pub(crate) mod abilities;
pub(crate) mod buildings;
pub(crate) mod components;
#[cfg(feature = "dev_view")]
pub(crate) mod dev_view;
pub(crate) mod dialogue;
pub(crate) mod fight;
pub(crate) mod forestry;
pub(crate) mod game_event_manager;
pub(crate) mod leaderboard;
pub(crate) mod leaderboard_menu;
pub(crate) mod level;
pub(crate) mod mana;
pub(crate) mod map;
pub(crate) mod movement;
pub(crate) mod net_receiver;
pub(crate) mod player_info;
pub(crate) mod quests;
pub(crate) mod religion_frame;
pub(crate) mod shaders;
pub(crate) mod status_effects;
pub(crate) mod story;
pub(crate) mod toplevel;
pub(crate) mod town;
pub(crate) mod town_resources;
pub(crate) mod units;
pub(crate) mod visits;

use self::player_info::PlayerState;
use crate::game::net_receiver::*;
use crate::gui::{input, sprites::*, ui_state::*};
use crate::init::loading::GameLoadingData;
use crate::net::NetMsg;
use crate::prelude::*;
use crate::{
    game::{components::*, player_info::PlayerInfo, town::TownContextManager},
    resolution::{SCREEN_H, SCREEN_W},
};
use chrono::NaiveDateTime;
use game_event_manager::GameEvent;
use map::{GlobalMap, GlobalMapPrivateState};
use movement::*;
use paddle::quicksilver_compat::*;
use paddle::*;
use shaders::Shaders;
use shred::{Fetch, FetchMut};
use specs::prelude::*;
use std::sync::mpsc::Receiver;
use town::{DefaultShop, Town};

pub(crate) struct Game {
    pub world: World,
    pub sprites: Sprites,
    pub shaders: Shaders,
    pub locale: TextDb,
    pub net: Receiver<NetMsg>,
    pub time_zero: NaiveDateTime,
    pub total_updates: u64,
    pub mouse: PointerTracker,
    pub map: Option<GlobalMapPrivateState>,
    pub town_context: TownContextManager,
    pub loaded_data: Option<LoadedData>,

    #[cfg(feature = "dev_view")]
    pub palette: bool,
    #[cfg(feature = "dev_view")]
    pub active_test: Option<Box<crate::game::dev_view::benchmark::TestData>>,
}

impl Game {
    pub fn load_game(
        display: &mut Display,
        sprites: Sprites,
        locale: TextDb,
        game_data: GameLoadingData,
        net_chan: Receiver<NetMsg>,
        loaded_data: LoadedData,
    ) -> PadlResult<Self> {
        let player_info = game_data.player_info;
        let town_context = TownContextManager::new(player_info.clone());
        let mut world = crate::init::init_world(player_info);
        let now = utc_now();
        let shaders = Shaders::load(display, &sprites);
        world.insert::<Now>(Now(now));

        world.maintain();
        let mut game = Game {
            world: world,
            sprites,
            shaders,
            locale,
            net: net_chan,
            time_zero: now,
            total_updates: 0,
            map: None,
            town_context,
            mouse: Default::default(),
            loaded_data: Some(loaded_data),
            #[cfg(feature = "dev_view")]
            palette: false,
            #[cfg(feature = "dev_view")]
            active_test: None,
        };
        game.prepare_town_resources();
        game.load_village_info(game_data.village_info)?;
        game.load_buildings_from_net_response(game_data.buildings_response)?;
        game.town_mut().refresh_attacker_direction();

        // Make sure buildings are loaded properly before inserting any types of units
        game.world.maintain();
        game.town_world_mut().maintain();
        game.world.write_resource::<PlayerState>().worker_population =
            Some(game_data.worker_response.len() as u32);
        load_workers_from_net_response(
            game.town_context.active_context_mut(),
            game_data.worker_response,
        );
        let n = load_hobos_from_net_response(
            game.town_context.active_context_mut(),
            game_data.hobos_response,
        )?;
        game.world.write_resource::<PlayerState>().hobo_population = Some(n);
        game.refresh_visitor_gate();
        game.load_attacking_hobos(game_data.attacking_hobos)?;
        game.load_player_info(game_data.player_info)?;

        game.world.maintain();
        game.town_world_mut().maintain();
        game.init_map();

        Ok(game)
    }
    /// To be called when game has just been started, after loading finished.
    /// Some game initialization requires event manager to be active already, this should be guaranteed by this point.
    pub fn post_load(&mut self) -> PadlResult<()> {
        // Make sure all units are loaded properly before story triggers are added
        self.world.maintain();
        self.town_world_mut().maintain();
        self.load_story_state()?;
        self.update_temple()?;
        Ok(())
    }

    pub fn main_update_loop(&mut self) -> PadlResult<()> {
        {
            self.map_mut().update();
        }
        self.update_time_reference();
        if self.total_updates % 300 == 15 {
            self.reaper();
        }
        self.world.maintain();
        Ok(())
    }
    pub fn init_map(&mut self) {
        let (private, shared) = GlobalMap::new();
        self.map = Some(private);
        self.world.insert(shared);
    }

    pub fn town(&self) -> Fetch<Town> {
        self.town_context.town()
    }
    pub fn town_mut(&self) -> FetchMut<Town> {
        self.town_context.town_mut()
    }
    pub fn map_mut(&mut self) -> GlobalMap {
        GlobalMap::combined(self.map.as_mut().unwrap(), self.world.write_resource())
    }
    pub fn player(&self) -> specs::shred::Fetch<PlayerState> {
        self.world.read_resource()
    }
    pub fn update_time_reference(&mut self) {
        if self.time_zero != NaiveDateTime::from_timestamp(0, 0) {
            let t = utc_now();
            self.world.insert(Now(t));
        }
    }
    /// Removes entities outside the map
    pub fn reaper(&mut self) {
        let map = Rectangle::new_sized((SCREEN_W, SCREEN_H));
        let p = self.town_world().read_storage::<Position>();
        let mut dead = vec![];
        for (entity, position) in (&self.town_world().entities(), &p).join() {
            if !position.area.overlaps_rectangle(&map) {
                dead.push(entity);
            }
        }
        std::mem::drop(p);
        self.town_world_mut()
            .delete_entities(&dead)
            .expect("Something bad happened when deleting dead entities");
    }
    fn worker_entity_by_net_id(&self, net_id: i64) -> PadlResult<Entity> {
        // TODO: Efficient NetId lookup
        let world = self.town_world();
        let net = world.read_storage::<NetObj>();
        let ent = world.entities();
        NetObj::lookup_worker(net_id, &net, &ent)
    }
    /// Transforms a PadlResult to an Option, handing errors to the error queue
    pub fn check<R>(&self, res: PadlResult<R>) -> Option<R> {
        if let Err(e) = res {
            nuts::publish(e);
            None
        } else {
            Some(res.unwrap())
        }
    }
    pub fn confirm_to_user(&mut self, text_key: TextKey) -> PadlResult<()> {
        const BLUE: Color = Color {
            r: 0.000,
            g: 0.059,
            b: 0.631,
            a: 1.0,
        };
        TextBoard::display_custom_message(
            self.locale.gettext(text_key.key()).to_owned(),
            BLUE,
            3_000,
        )?;
        Ok(())
    }
}

/// Deletes all building entities (lazy, requires world.maintain())
fn flush_buildings(world: &World) -> PadlResult<()> {
    let b = world.read_storage::<buildings::Building>();
    for (entity, _marker) in (&world.entities(), &b).join() {
        world
            .entities()
            .delete(entity)
            .map_err(|_| PadlError::dev_err(PadlErrorCode::EcsError("Delete building")))?;
    }
    Ok(())
}
/// Deletes all worker entities (lazy, requires world.maintain())
fn flush_workers(world: &World) -> PadlResult<()> {
    let w = world.read_storage::<units::workers::Worker>();
    for (entity, _marker) in (&world.entities(), &w).join() {
        world
            .entities()
            .delete(entity)
            .map_err(|_| PadlError::dev_err(PadlErrorCode::EcsError("Delete worker")))?;
    }
    Ok(())
}
/// Deletes all home hobo entities (lazy, requires world.maintain())
fn flush_home_hobos(world: &World) -> PadlResult<()> {
    let w = world.read_storage::<units::hobos::Hobo>();
    for (entity, _marker) in (&world.entities(), &w).join() {
        world
            .entities()
            .delete(entity)
            .map_err(|_| PadlError::dev_err(PadlErrorCode::EcsError("Delete hobo")))?;
    }
    Ok(())
}
/// Deletes all hobo entities (lazy, requires world.maintain())
#[allow(dead_code)] // Used for benchmarks
fn flush_hobos(world: &World) -> PadlResult<()> {
    let w = world.read_storage::<components::NetObj>();
    for (entity, netid) in (&world.entities(), &w).join() {
        if netid.is_hobo() {
            world
                .entities()
                .delete(entity)
                .map_err(|_| PadlError::dev_err(PadlErrorCode::EcsError("Delete hobo")))?;
        }
    }
    Ok(())
}
