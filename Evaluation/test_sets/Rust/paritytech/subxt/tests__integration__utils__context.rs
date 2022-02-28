// Copyright 2019-2022 Parity Technologies (UK) Ltd.
// This file is part of subxt.
//
// subxt is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// subxt is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with subxt.  If not, see <http://www.gnu.org/licenses/>.

pub use crate::{
    node_runtime,
    TestNodeProcess,
};

use sp_core::sr25519::Pair;
use sp_keyring::AccountKeyring;
use subxt::{
    extrinsic::ChargeAssetTxPayment,
    Client,
    DefaultConfig,
    DefaultExtraWithTxPayment,
    PairSigner,
};

/// substrate node should be installed on the $PATH
const SUBSTRATE_NODE_PATH: &str = "substrate";

pub type NodeRuntimeSignedExtra =
    DefaultExtraWithTxPayment<DefaultConfig, ChargeAssetTxPayment<DefaultConfig>>;

pub async fn test_node_process_with(
    key: AccountKeyring,
) -> TestNodeProcess<DefaultConfig> {
    let path = std::env::var("SUBSTRATE_NODE_PATH").unwrap_or_else(|_| {
        if which::which(SUBSTRATE_NODE_PATH).is_err() {
            panic!("A substrate binary should be installed on your path for integration tests. \
            See https://github.com/paritytech/subxt/tree/master#integration-testing")
        }
        SUBSTRATE_NODE_PATH.to_string()
    });

    let proc = TestNodeProcess::<DefaultConfig>::build(path.as_str())
        .with_authority(key)
        .scan_for_open_ports()
        .spawn::<DefaultConfig>()
        .await;
    proc.unwrap()
}

pub async fn test_node_process() -> TestNodeProcess<DefaultConfig> {
    test_node_process_with(AccountKeyring::Alice).await
}

pub struct TestContext {
    pub node_proc: TestNodeProcess<DefaultConfig>,
    pub api: node_runtime::RuntimeApi<DefaultConfig, NodeRuntimeSignedExtra>,
}

impl TestContext {
    pub fn client(&self) -> &Client<DefaultConfig> {
        &self.api.client
    }
}

pub async fn test_context() -> TestContext {
    env_logger::try_init().ok();
    let node_proc = test_node_process_with(AccountKeyring::Alice).await;
    let api = node_proc.client().clone().to_runtime_api();
    TestContext { node_proc, api }
}

pub fn pair_signer(
    pair: Pair,
) -> PairSigner<DefaultConfig, NodeRuntimeSignedExtra, Pair> {
    PairSigner::new(pair)
}
