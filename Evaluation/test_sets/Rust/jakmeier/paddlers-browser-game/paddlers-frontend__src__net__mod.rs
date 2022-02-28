pub mod ajax;
pub mod authentication;
pub mod game_master_api;
pub mod graphql;
pub mod state;
pub mod url;

use crate::game::player_info::PlayerInfo;
use game_master_api::RestApiState;
use graphql::{query_types::*, GraphQlState};
use paddle::web_integration::*;
use paddle::{Domain, NutsCheck};
use paddlers_shared_lib::prelude::VillageKey;
use wasm_bindgen::prelude::*;

use std::{future::Future, sync::mpsc::Sender};

use crate::prelude::*;

pub enum NetMsg {
    Attacks(AttacksResponse),
    Buildings(BuildingsResponse),
    Error(PadlError),
    Hobos(HobosQueryResponse, VillageKey),
    Leaderboard(usize, Vec<(String, i64)>, usize),
    Map(MapResponse, i32, i32),
    Player(PlayerInfo),
    VillageInfo(VolatileVillageInfoResponse),
    UpdateWorkerTasks(WorkerTasksResponse),
    Workers(WorkerResponse, VillageKey),
    Reports(ReportsResponse),
    Quests(QuestsResponse),
}

struct NetState {
    chan: Sender<NetMsg>,
    logged_in: bool,
    gql_state: GraphQlState,
    sync: graphql::SyncState,
}

const NET_THREAD_TIMEOUT_MS: i32 = 100;

// Requests
struct NetworkUpdate;
struct RequestPlayerUpdate;
struct RequestClientStateUpdate;
struct RequestResourceUpdate;
struct LoggedIn;
struct RequestQuests;
struct RequestLeaderboard {
    offset: i64,
    limit: i64,
}
struct RequestMapRead {
    min: i32,
    max: i32,
}
struct RequestWorkerTasksUpdate {
    unit_id: i64,
}
struct RequestForeignTownUpdate {
    vid: VillageKey,
}
struct RequestHobos;

// Update for responses
struct NewAttackId {
    id: i64,
}
struct NewReportId {
    id: i64,
}

/// Initializes state necessary for networking
pub fn init_net(chan: Sender<NetMsg>) {
    NetState::init(chan);
    RestApiState::init();
}

/// Initiates regular updates to sync with backend
pub fn start_sync() {
    NetState::start_working();
}

#[wasm_bindgen]
/// Sets up continuous networking with the help of JS setTimeout
/// Must be called from JS once the user is logged in
pub fn start_network_thread() {
    nuts::publish(LoggedIn);
}
/// Sends all requests out necessary for the client state to display a full game view including the home town
pub fn request_client_state() {
    nuts::publish(RequestClientStateUpdate);
}
pub fn request_map_read(min: i32, max: i32) {
    nuts::publish(RequestMapRead { min, max });
}
pub fn request_worker_tasks_update(unit_id: i64) {
    nuts::publish(RequestWorkerTasksUpdate { unit_id });
}
pub fn request_resource_update() {
    nuts::publish(RequestResourceUpdate);
}
pub fn request_player_update() {
    nuts::publish(RequestPlayerUpdate);
}
pub fn request_leaderboard(offset: i64, limit: i64) {
    nuts::publish(RequestLeaderboard { offset, limit });
}
pub fn request_foreign_town(vid: VillageKey) {
    nuts::publish(RequestForeignTownUpdate { vid });
}

impl NetState {
    fn init(chan: Sender<NetMsg>) {
        let ns = NetState {
            logged_in: false,
            chan,
            gql_state: GraphQlState::new(),
            sync: graphql::SyncState::new(),
        };
        let net_activity = nuts::new_domained_activity(ns, &Domain::Network);
        net_activity.subscribe(NetState::log_in);
        net_activity.subscribe(NetState::work);
        net_activity.subscribe(NetState::request_player_update);
        net_activity.subscribe(NetState::request_leaderboard);
        net_activity.subscribe(NetState::request_client_state);
        net_activity.subscribe(NetState::request_resource_update);
        net_activity.subscribe(NetState::request_map_read);
        net_activity.subscribe(NetState::request_worker_tasks_update);
        net_activity.subscribe(NetState::request_foreign_town);
        net_activity.subscribe(NetState::request_quests);
        net_activity.subscribe(NetState::request_hobos);
        net_activity.subscribe(NetState::update_attack_id);
        net_activity.subscribe(NetState::update_report_id);
        net_activity.subscribe(NetState::scheduled_update);
    }

    // For frequent updates.
    // This is called every time NetworkUpdate is being published, which happens every NET_THREAD_TIMEOUT_MS once loading has completed.
    fn work(&mut self, _: &NetworkUpdate) {
        self.sync_tick();
    }

    pub fn start_working() {
        let work_thread = start_thread(|| nuts::publish(NetworkUpdate), NET_THREAD_TIMEOUT_MS);
        nuts::store_to_domain(&Domain::Network, work_thread);
    }

    // Sends all requests out necessary for the client state to display a full game view including the home town
    // If the player is not logged in, yet, this function queues itself until the player is logged in.
    // Once logged in, the requests are sent exactly once.
    fn request_client_state(&mut self, _: &RequestClientStateUpdate) {
        if self.logged_in {
            self.transfer_response(GraphQlState::buildings_query());
            self.transfer_response(GraphQlState::workers_query());
            self.transfer_response(GraphQlState::hobos_query());
            self.transfer_response(self.gql_state.attacks_query());
            self.transfer_response(GraphQlState::resource_query());
            self.transfer_response(self.gql_state.reports_query()); // TODO: Don't load this for initial view
            self.transfer_response(GraphQlState::quests_query()); // TODO: Don't load this for initial view
            request_player_update();
        } else {
            let mut thread = paddle::web_integration::create_thread(request_client_state);
            thread.set_timeout(50).nuts_check();
            nuts::store_to_domain(&Domain::Network, (thread,));
        }
    }

    fn request_foreign_town(&mut self, msg: &RequestForeignTownUpdate) {
        self.transfer_response(GraphQlState::foreign_buildings_query(msg.vid));
        self.transfer_response(GraphQlState::foreign_hobos_query(msg.vid));
        // TODO: Other state
    }

    fn request_player_update(&mut self, _: &RequestPlayerUpdate) {
        if self.logged_in {
            self.transfer_response(GraphQlState::player_info_query());
        } else {
            let mut thread = paddle::web_integration::create_thread(request_player_update);
            thread.set_timeout(50).nuts_check();
            nuts::store_to_domain(&Domain::Network, (thread,));
        }
    }

    fn request_leaderboard(&mut self, msg: &RequestLeaderboard) {
        self.transfer_response(GraphQlState::leaderboard_query(msg.offset, msg.limit));
    }

    fn request_worker_tasks_update(&mut self, msg: &RequestWorkerTasksUpdate) {
        self.transfer_response(GraphQlState::worker_tasks_query(msg.unit_id));
    }

    fn request_resource_update(&mut self, _: &RequestResourceUpdate) {
        self.transfer_response(GraphQlState::resource_query());
    }

    fn request_map_read(&mut self, msg: &RequestMapRead) {
        self.transfer_response(GraphQlState::map_query(msg.min, msg.max));
    }

    fn request_quests(&mut self, _msg: &RequestQuests) {
        self.transfer_response(GraphQlState::quests_query());
    }

    fn request_hobos(&mut self, _msg: &RequestHobos) {
        self.transfer_response(GraphQlState::hobos_query());
    }

    fn log_in(&mut self, _: &LoggedIn) {
        self.logged_in = true;
    }

    fn update_attack_id(&mut self, msg: &NewAttackId) {
        self.gql_state.update_attack_id(msg.id);
    }
    fn update_report_id(&mut self, msg: &NewReportId) {
        self.gql_state.update_report_id(msg.id);
    }

    fn get_channel(&self) -> Sender<NetMsg> {
        self.chan.clone()
    }
    fn transfer_response<Q: Future<Output = PadlResult<NetMsg>> + 'static>(&self, query: Q) {
        let sender = self.get_channel();
        wasm_bindgen_futures::spawn_local(async move {
            let netmsg = match query.await {
                Ok(msg) => msg,
                Err(e) => {
                    // Do not send messge here through nuts, yet.
                    // The net receiver needs a chance to recover failures first.y
                    NetMsg::Error(e)
                }
            };
            sender.send(netmsg).expect("Transferring data to game");
        });
    }
}

impl std::fmt::Debug for NetMsg {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::Attacks(_) => write!(f, "NetMsg: Attacks"),
            Self::Buildings(_) => write!(f, "NetMsg: Buildings"),
            Self::Error(_) => write!(f, "NetMsg: Error"),
            Self::Hobos(_, _) => write!(f, "NetMsg: Hobos"),
            Self::Leaderboard(_, _, _) => write!(f, "NetMsg: Leaderboard"),
            Self::Map(_, _, _) => write!(f, "NetMsg: Map"),
            Self::Player(_) => write!(f, "NetMsg: Player"),
            Self::VillageInfo(_) => write!(f, "NetMsg: VillageInfo"),
            Self::UpdateWorkerTasks(_) => write!(f, "NetMsg: UpdateWorkerTasks"),
            Self::Workers(_, _) => write!(f, "NetMsg: Workers"),
            Self::Reports(_) => write!(f, "NetMsg: Reports"),
            Self::Quests(_) => write!(f, "NetMsg: Quests"),
        }
    }
}
