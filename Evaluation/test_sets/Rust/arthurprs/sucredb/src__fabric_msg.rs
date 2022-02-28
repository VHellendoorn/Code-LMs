use bytes::Bytes;
use cubes::Cube;
use database::*;
use version_vector::*;

#[derive(Debug, Copy, Clone)]
pub enum FabricMsgType {
    Crud,
    Synch,
    DHT,
    Unknown,
}

#[derive(Debug, Serialize, Deserialize, PartialEq, Eq)]
pub enum FabricError {
    NoRoute,
    CookieNotFound,
    BadVNodeStatus,
    NotReady,
    SyncInterrupted,
    StorageError,
}

#[derive(Debug, Serialize, Deserialize)]
pub enum FabricMsg {
    RemoteGet(MsgRemoteGet),
    RemoteGetAck(MsgRemoteGetAck),
    RemoteSet(MsgRemoteSet),
    RemoteSetAck(MsgRemoteSetAck),
    SyncStart(MsgSyncStart),
    SyncSend(MsgSyncSend),
    SyncAck(MsgSyncAck),
    SyncFin(MsgSyncFin),
    DHTAE(VersionVector),
    DHTSync(Bytes),
    Unknown,
}

#[derive(Debug, Serialize)]
pub enum FabricMsgRef<'a> {
    RemoteGet(&'a MsgRemoteGet),
    RemoteGetAck(&'a MsgRemoteGetAck),
    RemoteSet(&'a MsgRemoteSet),
    RemoteSetAck(&'a MsgRemoteSetAck),
    SyncStart(&'a MsgSyncStart),
    SyncSend(&'a MsgSyncSend),
    SyncAck(&'a MsgSyncAck),
    SyncFin(&'a MsgSyncFin),
    DHTAE(&'a VersionVector),
    DHTSync(&'a Bytes),
    Unknown,
}

impl FabricMsg {
    pub fn get_type(&self) -> FabricMsgType {
        match *self {
            FabricMsg::RemoteGet(..)
            | FabricMsg::RemoteGetAck(..)
            | FabricMsg::RemoteSet(..)
            | FabricMsg::RemoteSetAck(..) => FabricMsgType::Crud,
            FabricMsg::SyncStart(..)
            | FabricMsg::SyncSend(..)
            | FabricMsg::SyncAck(..)
            | FabricMsg::SyncFin(..) => FabricMsgType::Synch,
            FabricMsg::DHTSync(..) | FabricMsg::DHTAE(..) => FabricMsgType::DHT,
            _ => unreachable!(),
        }
    }
}

impl<'a> FabricMsgRef<'a> {
    pub fn get_type(&self) -> FabricMsgType {
        match *self {
            FabricMsgRef::RemoteGet(..)
            | FabricMsgRef::RemoteGetAck(..)
            | FabricMsgRef::RemoteSet(..)
            | FabricMsgRef::RemoteSetAck(..) => FabricMsgType::Crud,
            FabricMsgRef::SyncStart(..)
            | FabricMsgRef::SyncSend(..)
            | FabricMsgRef::SyncAck(..)
            | FabricMsgRef::SyncFin(..) => FabricMsgType::Synch,
            FabricMsgRef::DHTSync(..) | FabricMsgRef::DHTAE(..) => FabricMsgType::DHT,
            _ => unreachable!(),
        }
    }
}

#[derive(Debug, Serialize, Deserialize)]
pub struct MsgRemoteGet {
    pub vnode: VNodeNo,
    pub cookie: Cookie,
    pub keys: Vec<Bytes>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct MsgRemoteGetAck {
    pub vnode: VNodeNo,
    pub cookie: Cookie,
    pub result: Result<Vec<Cube>, FabricError>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct MsgRemoteSet {
    pub vnode: VNodeNo,
    pub cookie: Cookie,
    pub writes: Vec<(Bytes, Cube, bool)>,
    pub reply: bool,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct MsgRemoteSetAck {
    pub vnode: VNodeNo,
    pub cookie: Cookie,
    pub result: Result<Vec<Option<Cube>>, FabricError>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct MsgSyncStart {
    pub vnode: VNodeNo,
    pub cookie: Cookie,
    pub clocks_in_peer: BitmappedVersionVector,
    pub target: Option<NodeId>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct MsgSyncFin {
    pub vnode: VNodeNo,
    pub cookie: Cookie,
    pub result: Result<BitmappedVersionVector, FabricError>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct MsgSyncSend {
    pub vnode: VNodeNo,
    pub cookie: Cookie,
    pub seq: u64,
    pub key: Bytes,
    pub value: Cube,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct MsgSyncAck {
    pub vnode: VNodeNo,
    pub cookie: Cookie,
    pub seq: u64,
}

impl<'a> Into<FabricMsgRef<'a>> for &'a FabricMsg {
    fn into(self) -> FabricMsgRef<'a> {
        match self {
            &FabricMsg::RemoteGet(ref a) => FabricMsgRef::RemoteGet(a),
            &FabricMsg::RemoteGetAck(ref a) => FabricMsgRef::RemoteGetAck(a),
            &FabricMsg::RemoteSet(ref a) => FabricMsgRef::RemoteSet(a),
            &FabricMsg::RemoteSetAck(ref a) => FabricMsgRef::RemoteSetAck(a),
            &FabricMsg::SyncStart(ref a) => FabricMsgRef::SyncStart(a),
            &FabricMsg::SyncSend(ref a) => FabricMsgRef::SyncSend(a),
            &FabricMsg::SyncAck(ref a) => FabricMsgRef::SyncAck(a),
            &FabricMsg::SyncFin(ref a) => FabricMsgRef::SyncFin(a),
            &FabricMsg::DHTSync(ref a) => FabricMsgRef::DHTSync(a),
            &FabricMsg::DHTAE(ref a) => FabricMsgRef::DHTAE(a),
            _ => unreachable!(),
        }
    }
}

macro_rules! impl_into {
    ($w:ident, $msg:ident) => {
        impl Into<FabricMsg> for $msg {
            fn into(self) -> FabricMsg {
                FabricMsg::$w(self)
            }
        }
        impl<'a> Into<FabricMsgRef<'a>> for &'a $msg {
            fn into(self) -> FabricMsgRef<'a> {
                FabricMsgRef::$w(self)
            }
        }
    };
}

impl_into!(RemoteGet, MsgRemoteGet);
impl_into!(RemoteGetAck, MsgRemoteGetAck);
impl_into!(RemoteSet, MsgRemoteSet);
impl_into!(RemoteSetAck, MsgRemoteSetAck);
impl_into!(SyncAck, MsgSyncAck);
impl_into!(SyncSend, MsgSyncSend);
impl_into!(SyncFin, MsgSyncFin);
impl_into!(SyncStart, MsgSyncStart);
