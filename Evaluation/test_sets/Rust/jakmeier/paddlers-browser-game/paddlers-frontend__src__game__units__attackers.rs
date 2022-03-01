mod visitor_maintenance;

use chrono::NaiveDateTime;
pub use visitor_maintenance::*;

use crate::gui::ui_state::Now;
use crate::gui::{render::Renderable, sprites::*, utils::*, z::Z_VISITOR};
use crate::net::graphql::query_types::HoboEffect;
use crate::prelude::*;
use crate::{game::town::town_defence::AttackingHobo, resolution::TOWN_TILE_S};
use crate::{
    game::{
        components::{AnimationState, NetObj},
        fight::Health,
        input::Clickable,
        movement::{Moving, Position, TargetPosition},
        status_effects::StatusEffects,
        town::visitor_gate::{GraphqlVisitingHobo, WaitingAttack},
        visits::attacks::Attack,
    },
    gui::animation::IDirection,
};
use paddle::Vector;
use paddlers_shared_lib::graphql_types::*;
use paddlers_shared_lib::{game_mechanics::town::*, prelude::AttackKey};
use specs::prelude::*;

const ATTACKER_SIZE_FACTOR_X: f32 = 0.6;
const ATTACKER_SIZE_FACTOR_Y: f32 = 0.4;

#[derive(Debug, Component)]
#[storage(HashMapStorage)]
/// A visitor is an attacking hobo that has entered the village
pub struct Visitor {
    pub hurried: bool,
    pub speed: f32,
    pub entered_village: NaiveDateTime,
    pub rank_offset: usize,
}

#[cfg(feature = "dev_view")]
pub fn insert_duck(
    world: &mut World,
    pos: impl Into<Vector>,
    color: UnitColor,
    birth_time: NaiveDateTime,
    speed: impl Into<Vector>,
    hp: i64,
    ul: f32,
    netid: i64,
    effects: &[HoboEffect],
    final_pos: Option<Vector>,
) -> PadlResult<Entity> {
    let mut builder = world.create_entity();
    let pos = pos.into();
    let speed: Vector = speed.into();
    builder = builder.with(Moving::new(birth_time, pos, speed, speed.len()));
    if let Some(pos) = final_pos {
        builder = builder.with(TargetPosition::new(pos));
    }

    build_new_duck_entity(
        builder,
        pos,
        color,
        birth_time,
        speed.len(),
        Health::new_full_health(hp),
        ul,
        netid,
        effects,
        false,
        0,
    )
    .map(specs::EntityBuilder::build)
}

pub fn build_new_duck_entity<'a>(
    builder: specs::EntityBuilder<'a>,
    pos: impl Into<Vector>,
    color: UnitColor,
    entered_village: NaiveDateTime,
    speed: f32,
    hp: Health,
    netid: i64,
    effects: &[HoboEffect],
    hurried: bool,
    rank_offset: usize,
) -> PadlResult<specs::EntityBuilder<'a>> {
    let size: Vector = Vector::new(
        ATTACKER_SIZE_FACTOR_X * TOWN_TILE_S as f32,
        ATTACKER_SIZE_FACTOR_Y * TOWN_TILE_S as f32,
    )
    .into();
    let status_effects = StatusEffects::from_gql_query(effects)?;
    let mut renderable = Renderable::new(RenderVariant::ImgWithImgBackground(
        SpriteSet::Simple(hobo_sprite_sad(color)),
        SingleSprite::Water,
    ));
    if hp.hp == 0 {
        change_duck_sprite_to_happy(&mut renderable);
    }
    let pos = pos.into();
    let builder = builder
        .with(Position::new(pos, size, Z_VISITOR))
        .with(renderable)
        .with(Clickable)
        .with(status_effects)
        .with(NetObj::hobo(netid))
        .with(Visitor {
            hurried,
            speed,
            entered_village,
            rank_offset,
        })
        .with(hp);

    Ok(builder)
}

use crate::net::graphql::attacks_query::AttacksQueryVillageAttacks;
impl AttacksQueryVillageAttacks {
    pub(crate) fn create_entities<'a, 'b>(self, game: &mut Game) -> PadlResult<Vec<Entity>> {
        let arrival = GqlTimestamp::from_string(&self.arrival)
            .unwrap()
            .to_chrono();

        let description = self
            .attacker
            .as_ref()
            .map(|a| &a.display_name)
            .map(|player| format!("From {}", player))
            .unwrap_or("Anarchists".to_owned());
        let size = self.units.len() as u32;
        let atk = Attack::new(arrival, description, size);

        let out;

        // Either create active attackers (AttackingHobo) and insert them as entities, or create a queued attack and store it in the visitor gate
        if let Some(entered) = self.entered_village {
            let birth_time = GqlTimestamp::from_string(&entered).unwrap().to_chrono();
            out = game.insert_visitors_from_active_attack(self.units, birth_time)?;
        } else {
            let now = paddle::utc_now();
            let arrived = arrival <= now;
            let key = AttackKey(self.id.parse().expect("Parsing id"));
            let waiting_attack = WaitingAttack::new(arrival, self.units, arrived, key);
            game.queue_attack(waiting_attack);
            out = vec![];
        }

        game.world.create_entity().with(atk).build();

        Ok(out)
    }
}

impl Game {
    pub fn insert_visitors_from_active_attack(
        &mut self,
        units: Vec<GraphqlVisitingHobo>,
        start_of_fight: NaiveDateTime,
    ) -> PadlResult<Vec<Entity>> {
        let mut out = vec![];
        let now = self.world.fetch::<Now>().0;
        for (i, unit) in units.into_iter().enumerate() {
            let unit_rep = AttackingHobo {
                unit,
                start_of_fight: start_of_fight.into(),
            };
            let effects = self.touched_auras(&unit_rep, now.into());
            let direction = self.town().attacker_direction;
            let builder = unit_rep.create_entity(
                self.town_context.home_world_mut().create_entity(),
                now,
                start_of_fight,
                i,
                effects,
                direction,
            )?;
            out.push(builder.build());
        }
        Ok(out)
    }
}

#[derive(Clone, Copy)]
pub enum AttackerDirection {
    LeftToRight,
    RightToLeft,
}
impl AttackerDirection {
    pub fn adjust_movement(self, v: Vector) -> Vector {
        match self {
            AttackerDirection::LeftToRight => v,
            AttackerDirection::RightToLeft => -v,
        }
    }
    pub fn origin_position(self) -> Vector {
        let y = TOWN_LANE_Y as f32 * TOWN_TILE_S as f32;
        let x;
        match self {
            AttackerDirection::LeftToRight => {
                x = -0.5 * TOWN_TILE_S as f32;
            }
            AttackerDirection::RightToLeft => {
                x = (TOWN_X as f32 - 0.5) * TOWN_TILE_S as f32;
            }
        }
        (x, y).into()
    }
}
impl AttackingHobo {
    fn create_entity<'a>(
        &self,
        mut builder: specs::EntityBuilder<'a>,
        now: NaiveDateTime,
        birth: NaiveDateTime,
        pos_rank: usize,
        auras: Vec<(<Game as IDefendingTown>::AuraId, i32)>,
        direction: AttackerDirection,
    ) -> PadlResult<specs::EntityBuilder<'a>> {
        let ul = TOWN_TILE_S as f32;
        let v = self.unit.hobo.speed as f32 * ul;
        let mut pos = direction.origin_position() + attacker_position_rank_offset(pos_rank, ul);
        let mut t0 = birth;
        let hp = self.unit.hobo.hp;
        let netid = self.unit.hobo.id.parse().expect("Parsing id");
        let color = self.unit.hobo.color.unwrap_or(UnitColor::Yellow);
        let time_until_resting = self.time_until_resting().as_duration();

        // Simulate all interactions with buildings for the visitor which happened in the past
        let dmg = <Game as IDefendingTown>::damage(&auras) + self.effects_strength();
        let hp_left = (hp - dmg as i64).max(0);
        let aura_ids = auras.into_iter().map(|a| a.0).collect();
        let health = Health::new(hp, hp_left, aura_ids);

        // Adapt position for units that have been resting and were then released
        if let Some(released) = &self.unit.info.released {
            let released = GqlTimestamp::from_string(released).unwrap().to_chrono();
            if released > birth + time_until_resting {
                pos.x = TOWN_RESTING_X as f32 * ul;
                t0 = released;
            }
        }

        // Insert components for movement (unless visitor is currently resting)
        let can_rest = !self.unit.hobo.hurried && self.unit.info.released.is_none();
        let resting = can_rest && birth + time_until_resting <= now;
        let movement = direction.adjust_movement(Vector::new(v, 0.0));
        if !resting {
            builder = builder.with(Moving::new(t0, pos, movement, v));
            if can_rest {
                let final_pos = Vector::new(TOWN_RESTING_X as f32 * ul, pos.y);
                builder = builder.with(TargetPosition::new(final_pos));
            }
        } else {
            pos.x = TOWN_RESTING_X as f32 * ul;
        }
        let builder = builder.with(AnimationState::new(Direction::from_vector(&movement)));

        build_new_duck_entity(
            builder,
            pos,
            color,
            birth,
            v,
            health,
            netid,
            &self.unit.hobo.effects,
            self.unit.hobo.hurried,
            pos_rank,
        )
    }
}

fn attacker_position_rank_offset(pr: usize, ul: f32) -> Vector {
    let y = if pr % 2 == 1 { ul * 0.5 } else { 0.0 };
    let x = ul * 0.3 * pr as f32;
    (x, y).into()
}

pub fn hobo_sprite_sad(color: UnitColor) -> SingleSprite {
    match color {
        UnitColor::Yellow => SingleSprite::Duck,
        UnitColor::White => SingleSprite::WhiteDuck,
        UnitColor::Camo => SingleSprite::CamoDuck,
        UnitColor::Prophet => SingleSprite::Prophet,
    }
}
pub fn hobo_sprite_happy(color: UnitColor) -> SingleSprite {
    match color {
        UnitColor::Yellow => SingleSprite::DuckHappy,
        UnitColor::White => SingleSprite::WhiteDuckHappy,
        UnitColor::Camo => SingleSprite::CamoDuckHappy,
        UnitColor::Prophet => SingleSprite::Prophet,
    }
}
