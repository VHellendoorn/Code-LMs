//! Shared Key
//!
//! Key shares of co-owned keys between user and server.

use super::super::{ecdsa, ClientShim, Result};
use shared_lib::{
    structs::{PrepareSignTxMsg, Protocol},
    Root,
};

use bitcoin::secp256k1::key::SecretKey;
use curv::elliptic::curves::traits::ECScalar;
use curv::FE;
use kms::ecdsa::two_party::MasterKey2;
use monotree::Proof;
use uuid::Uuid;

#[derive(Serialize, Deserialize, Clone)]
pub struct InclusionProofSMT {
    pub root: Root,
    pub proof: Option<Proof>,
}
#[derive(Serialize, Deserialize)]
pub struct SharedKey {
    pub id: Uuid,
    pub share: MasterKey2,
    pub value: u64, //Satoshis
    pub statechain_id: Option<Uuid>,
    pub tx_backup_psm: Option<PrepareSignTxMsg>, // back up transaction data
    pub proof_key: Option<String>,
    pub smt_proof: Option<InclusionProofSMT>,
    pub unspent: bool,
    pub funding_txid: String,
}

impl SharedKey {
    pub fn new(
        id: &Uuid,
        client_shim: &ClientShim,
        secret_key: &SecretKey,
        value: &u64,
        protocol: Protocol,
        solution: String,
    ) -> Result<SharedKey> {
        Self::new_repeat_keygen(id, client_shim, secret_key, value, protocol, solution, 0)
    }

    pub fn new_repeat_keygen(
        id: &Uuid,
        client_shim: &ClientShim,
        secret_key: &SecretKey,
        value: &u64,
        protocol: Protocol,
        solution: String,
        keygen_reps: u32
    ) -> Result<SharedKey> {
        let mut key_share_priv: FE = ECScalar::zero(); // convert to curv lib
        key_share_priv.set_element(*secret_key);
        ecdsa::get_master_key_repeat_keygen(id, client_shim, &key_share_priv, value, protocol, solution, keygen_reps)
    }

    pub fn add_proof_data(
        &mut self,
        proof_key: &String,
        root: &Root,
        proof: &Option<Proof>,
        funding_txid: &String,
    ) {
        self.proof_key = Some(proof_key.to_owned());
        self.smt_proof = Some(InclusionProofSMT {
            root: root.clone(),
            proof: proof.clone(),
        });
        self.funding_txid = funding_txid.clone();
    }

    pub fn update_proof(&mut self, root: &Root, proof: &Option<Proof>) {
        self.smt_proof = Some(InclusionProofSMT {
            root: root.clone(),
            proof: proof.clone(),
        });
    }
}
