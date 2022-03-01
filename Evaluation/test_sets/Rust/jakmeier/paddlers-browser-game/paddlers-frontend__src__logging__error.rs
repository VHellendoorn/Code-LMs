use js_sys::Object;
use paddle::{ErrorMessage, JsError};
use paddlers_shared_lib::prelude::PadlApiError;
use wasm_bindgen::JsValue;

use crate::game::town::{TileIndex, TileType};
use crate::prelude::*;
use std::fmt;
use std::sync::mpsc::SendError;

pub type PadlResult<R> = Result<R, PadlError>;

#[derive(Debug)]
pub struct PadlError {
    pub err: PadlErrorCode,
    pub(super) channel: ErrorChannel,
}

#[derive(Debug, Clone, Copy)]
pub(super) enum ErrorChannel {
    UserFacing,
    Technical,
}

impl PadlError {
    fn new(err: PadlErrorCode, chan: ErrorChannel) -> PadlError {
        PadlError {
            err: err,
            channel: chan,
        }
    }
    pub fn user_err(err: PadlErrorCode) -> PadlError {
        PadlError::new(err, ErrorChannel::UserFacing)
    }
    pub fn dev_err(err: PadlErrorCode) -> PadlError {
        PadlError::new(err, ErrorChannel::Technical)
    }
}

impl std::error::Error for PadlError {}
impl fmt::Display for PadlError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.err)
    }
}

impl PadlErrorCode {
    pub fn usr<R>(self) -> PadlResult<R> {
        Err(PadlError::user_err(self))
    }
    pub fn dev<R>(self) -> PadlResult<R> {
        Err(PadlError::dev_err(self))
    }
}

#[derive(Debug)]
pub enum PadlErrorCode {
    #[allow(dead_code)]
    TestError,
    // User
    BuildingFull(Option<BuildingType>),
    ForestTooSmall(usize),
    NotEnoughResources,
    NotEnoughSupply,
    NotEnoughMana,
    NotEnoughKarma(i64),
    NotEnoughUnits,
    NotReadyYet,
    PathBlocked,
    NoNetwork,
    NestEmpty,
    AbilityLocked,
    // Dev only
    DevMsg(&'static str),
    MapOverflow(TileIndex),
    NoStateForTile(TileIndex),
    UnexpectedTileType(&'static str, TileType),
    MissingComponent(&'static str),
    EcsError(&'static str),
    SpecsError(specs::error::Error),
    EventPoolSend(SendError<GameEvent>),
    RestAPI(String),
    InvalidGraphQLData(&'static str),
    GraphQlNoDataOrErrors,
    GraphQlGenericResponseError(String),
    GraphQlResponseError(PadlApiError),
    UnknownNetObj(crate::game::components::NetObj),
    InvalidDom(&'static str),
    PaddleError(String),
    DivError(String),
    JsonParseError(serde_json::error::Error),
    RonParseError(ron::error::Error),
    UrlParseError(String),
    BrowserError(String),
    #[allow(dead_code)]
    DialogueEmpty,
    #[allow(dead_code)]
    AuthorizationRequired,
    DataForInactiveTownReceived(&'static str),
}

impl fmt::Display for PadlErrorCode {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            PadlErrorCode::TestError => write!(f, "This is only used for testing"),
            // User
            PadlErrorCode::BuildingFull(Some(b)) => write!(f, "The {} is full.", b),
            PadlErrorCode::BuildingFull(None) => write!(f, "Building is full."),
            PadlErrorCode::ForestTooSmall(amount) => {
                write!(f, "Missing {} forest flora size.", amount)
            }
            PadlErrorCode::NotReadyYet => write!(f, "Patience! This is not ready, yet."),
            PadlErrorCode::NotEnoughResources => write!(f, "Need more resources."),
            PadlErrorCode::NotEnoughSupply => write!(f, "Requires more supplies."),
            PadlErrorCode::NotEnoughMana => write!(f, "Not enough mana."),
            PadlErrorCode::NotEnoughKarma(required) => {
                write!(f, "Requires at least {} karma.", required)
            }
            PadlErrorCode::NotEnoughUnits => write!(f, "Require more units."),
            PadlErrorCode::PathBlocked => write!(f, "The path is blocked."),
            PadlErrorCode::NoNetwork => write!(f, "Connection to server dropped."),
            PadlErrorCode::NestEmpty => write!(f, "Nobody around to invite."),
            PadlErrorCode::AbilityLocked => {
                write!(f, "Your Paddlers have not learned to do this, yet.")
            }
            // Dev
            PadlErrorCode::DevMsg(msg) => write!(f, "Dev Error Msg: {}", msg),
            PadlErrorCode::MapOverflow(i) => write!(f, "Index is outside the map: {:?}", i),
            PadlErrorCode::NoStateForTile(i) => write!(f, "No state found for tile: {:?}", i),
            PadlErrorCode::UnexpectedTileType(expected, was) => write!(
                f,
                "Unexpected tile type: Expected {} but was {:?}",
                expected, was
            ),
            PadlErrorCode::MissingComponent(component) => {
                write!(f, "Entity does not have required component: {}", component)
            }
            PadlErrorCode::EcsError(component) => write!(f, "ECS error: {}", component),
            PadlErrorCode::SpecsError(component) => write!(f, "Specs error: {}", component),
            PadlErrorCode::EventPoolSend(e) => write!(f, "EventPool send error: {}", e),
            PadlErrorCode::RestAPI(msg) => write!(f, "A REST API error occurred: {}", msg),
            PadlErrorCode::InvalidGraphQLData(reason) => {
                write!(f, "GraphQL query result has invalid data: {}", reason)
            }
            PadlErrorCode::GraphQlNoDataOrErrors => {
                write!(f, "GraphQL response contains no data and no errors.")
            }
            PadlErrorCode::GraphQlGenericResponseError(s) => {
                write!(f, "GraphQL response error: {}", s)
            }
            PadlErrorCode::GraphQlResponseError(code) => write!(f, "GraphQL API error: {}", code),
            PadlErrorCode::UnknownNetObj(key) => {
                write!(f, "GraphQL query result has unknown key: {:?}", key)
            }
            PadlErrorCode::InvalidDom(cause) => write!(f, "DOM error: {}", cause),
            PadlErrorCode::PaddleError(cause) => write!(f, "Paddle error: {}", cause),
            PadlErrorCode::DivError(cause) => write!(f, "Panes error: {}", cause),
            PadlErrorCode::JsonParseError(cause) => {
                write!(f, "Error while parsing JSON data: {}", cause)
            }
            PadlErrorCode::RonParseError(cause) => {
                write!(f, "Error while parsing RON data: {}", cause)
            }
            PadlErrorCode::UrlParseError(cause) => {
                write!(f, "Error while parsing browser URL: {}", cause)
            }
            PadlErrorCode::BrowserError(s) => write!(f, "Unexpected browser error: {}", s),
            PadlErrorCode::DialogueEmpty => write!(f, "No scene loaded in dialogue"),
            PadlErrorCode::AuthorizationRequired => {
                write!(f, "The requested resource permits authorized access only.")
            }
            PadlErrorCode::DataForInactiveTownReceived(data) => {
                write!(f, "Received data {} for town that is not active.", data)
            }
        }
    }
}

impl From<serde_json::error::Error> for PadlError {
    fn from(error: serde_json::error::Error) -> Self {
        PadlError::dev_err(PadlErrorCode::JsonParseError(error))
    }
}

impl From<url::ParseError> for PadlError {
    fn from(error: url::ParseError) -> Self {
        PadlError::dev_err(PadlErrorCode::UrlParseError(format!("{}", error)))
    }
}
impl From<ErrorMessage> for PadlError {
    fn from(error: ErrorMessage) -> Self {
        PadlError::dev_err(PadlErrorCode::PaddleError(error.text.to_string()))
    }
}
impl From<div::DivError> for PadlError {
    fn from(error: div::DivError) -> Self {
        PadlError::dev_err(PadlErrorCode::DivError(error.to_string()))
    }
}
impl From<SendError<GameEvent>> for PadlError {
    fn from(error: SendError<GameEvent>) -> Self {
        PadlError::dev_err(PadlErrorCode::EventPoolSend(error))
    }
}
impl From<specs::error::Error> for PadlError {
    fn from(error: specs::error::Error) -> Self {
        PadlError::dev_err(PadlErrorCode::SpecsError(error))
    }
}
impl From<&'static str> for PadlError {
    fn from(msg: &'static str) -> Self {
        PadlError::dev_err(PadlErrorCode::DevMsg(msg))
    }
}

impl From<JsValue> for PadlError {
    fn from(err: JsValue) -> Self {
        let obj: Object = err.into();
        let msg = obj.to_string().as_string().unwrap();
        PadlError::dev_err(PadlErrorCode::BrowserError(msg))
    }
}

impl From<JsError> for PadlError {
    fn from(err: JsError) -> Self {
        let obj: Object = err.0.into();
        let msg = obj.to_string().as_string().unwrap();
        PadlError::dev_err(PadlErrorCode::BrowserError(msg))
    }
}

impl From<paddlers_shared_lib::game_mechanics::town::TownError> for PadlError {
    fn from(error: paddlers_shared_lib::game_mechanics::town::TownError) -> Self {
        use paddlers_shared_lib::game_mechanics::town::TownError;
        match error {
            TownError::BuildingFull => PadlError::user_err(PadlErrorCode::BuildingFull(None)),
            TownError::NotEnoughSupply => PadlError::user_err(PadlErrorCode::NotEnoughSupply),
            TownError::InvalidState(s) => PadlError::dev_err(PadlErrorCode::DevMsg(s)),
        }
    }
}
