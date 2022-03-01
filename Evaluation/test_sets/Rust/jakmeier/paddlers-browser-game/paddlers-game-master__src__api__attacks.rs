use super::{check_owns_village0, internal_server_error};
use crate::game_master::attack_funnel::PlannedAttack;
use crate::game_master::event::Event;
use crate::game_master::town_worker::TownWorkerEventMsg;
use crate::{authentication::Authentication, game_master::story_worker::StoryWorkerMessage};
use actix::prelude::*;
use actix_web::error::BlockingError;
use actix_web::Responder;
use actix_web::{web, HttpResponse};
use futures::future::join_all;
use paddlers_shared_lib::{
    api::attacks::{AttackDescriptor, InvitationDescriptor, StartFightRequest},
    civilization::CivilizationPerk,
};
use paddlers_shared_lib::{prelude::*, story::story_trigger::StoryTrigger};

pub(crate) fn create_attack(
    pool: web::Data<crate::db::Pool>,
    actors: web::Data<crate::ActorAddresses>,
    body: web::Json<AttackDescriptor>,
    auth: Authentication,
) -> impl Future<Item = HttpResponse, Error = actix_web::Error> {
    let pool0 = pool.clone();
    let pool1 = pool.clone();
    let attack = body.0;
    let (x, y) = attack.to;
    let from_key = attack.from;
    let home_id = from_key.num();
    let attack_funnel = actors.attack_funnel.clone();

    let future_hobos = attack
        .units
        .into_iter()
        .map(move |hobo_key| {
            let db: crate::db::DB = pool.clone().get_ref().into();
            web::block(move || match db.hobo(hobo_key) {
                Some(hobo) => Ok(hobo),
                None => Err("Invalid hobo"),
            })
            .map_err(|e: BlockingError<_>| match e {
                BlockingError::Error(msg) => HttpResponse::Forbidden().body(msg).into(),
                BlockingError::Canceled => internal_server_error("Canceled"),
            })
            .and_then(move |hobo| {
                if hobo.home != home_id {
                    Err(HttpResponse::Forbidden()
                        .body("Hobo not from this village")
                        .into())
                } else {
                    Ok(hobo)
                }
            })
        })
        .collect::<Vec<_>>();
    let future_hobos = join_all(future_hobos);

    let future_villages = web::block(move || {
        let db: crate::db::DB = pool0.get_ref().into();
        check_owns_village0(&db, &auth, from_key)?;
        let destination = db.village_at(x as f32, y as f32);
        if destination.is_none() {
            Err("Invalid target village".to_owned())
        } else {
            Ok(destination.unwrap())
        }
    })
    .map_err(|e: BlockingError<std::string::String>| match e {
        BlockingError::Error(msg) => HttpResponse::Forbidden().body(msg).into(),
        BlockingError::Canceled => internal_server_error("Canceled"),
    })
    .and_then(move |target_village| {
        let db: crate::db::DB = pool1.get_ref().into();
        if let Some(origin_village) = db.village(from_key) {
            Ok((origin_village, target_village))
        } else {
            Err(internal_server_error("Owned village doesn't exist"))
        }
    });
    let joined = future_hobos.join(future_villages);
    joined
        .map(
            |(hobos, (origin_village, destination_village))| PlannedAttack {
                origin_village: Some(origin_village),
                destination_village,
                hobos,
                fixed_travel_time_s: None,
                subject_to_visitor_queue_limit: false,
            },
        )
        .and_then(move |pa| attack_funnel.try_send(pa).map_err(internal_server_error))
        .map(|()| HttpResponse::Ok().into())
}

pub(crate) fn welcome_visitor(
    pool: web::Data<crate::db::Pool>,
    body: web::Json<StartFightRequest>,
    mut auth: Authentication,
    addr: web::Data<crate::ActorAddresses>,
) -> HttpResponse {
    let db: crate::db::DB = pool.get_ref().into();
    let destination_village = body.destination;
    let attack = body.attack;
    if !db.village_owned_by(destination_village, auth.user.uuid) {
        return HttpResponse::Forbidden().body("Village not owned by player");
    }
    if let Some(player) = auth.player_object(&db) {
        addr.story_worker.do_send(StoryWorkerMessage::new_verified(
            player.key(),
            player.story_state,
            StoryTrigger::LetVisitorIn,
        ));
    }
    db.start_fight(attack, Some(destination_village));
    HttpResponse::Ok().into()
}
pub(crate) fn visitor_satisfied_notification(
    body: web::Json<HoboKey>,
    addr: web::Data<crate::ActorAddresses>,
) -> impl Responder {
    let event = Event::CheckVisitorHp { hobo_id: body.0 };
    addr.town_worker
        .try_send(TownWorkerEventMsg(event, chrono::Utc::now()))
        .map_err(|e| eprintln!("Send failed: {:?}", e))
}

pub(crate) fn new_invitation(
    pool: web::Data<crate::db::Pool>,
    body: web::Json<InvitationDescriptor>,
    mut auth: Authentication,
    addr: web::Data<crate::ActorAddresses>,
) -> impl Future<Item = HttpResponse, Error = actix_web::Error> {
    web::block(move || {
        // Check that request is valid and forward request to actor
        let db: crate::db::DB = pool.get_ref().into();
        let origin_vid = db.building(body.nest).ok_or("Nest not found")?.village();
        let origin_village = db.village(origin_vid);
        let destination_village = db.village(body.to).ok_or("Village not found")?;
        let hobos = db.idle_hobos_in_nest(body.nest);
        if !auth
            .player_object(&db)
            .ok_or_else(|| "No such player".to_owned())?
            .civilization_perks()
            .has(CivilizationPerk::Invitation)
        {
            return Err("Invitations not unlocked".to_owned());
        }
        if !db.village_owned_by(destination_village.key(), auth.user.uuid) {
            return Err("Village not owned by player".to_owned());
        }
        let atk = PlannedAttack {
            origin_village,
            destination_village,
            hobos,
            fixed_travel_time_s: None,
            subject_to_visitor_queue_limit: true,
        };
        addr.attack_funnel
            .try_send(atk)
            .map_err(|e| format!("Spawning attack failed: {:?}", e))
    })
    .then(
        |result: Result<(), BlockingError<std::string::String>>| match result {
            Err(BlockingError::Error(msg)) => Ok(HttpResponse::Forbidden().body(msg).into()),
            Err(BlockingError::Canceled) => Ok(HttpResponse::InternalServerError().into()),
            Ok(()) => Ok(HttpResponse::Ok().into()),
        },
    )
}
