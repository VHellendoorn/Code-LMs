/// test interactions of withdraw_relay state machine with RPC

extern crate futures;
#[macro_use]
extern crate serde_json;
extern crate bridge;
#[macro_use]
extern crate tests;
extern crate ethabi;
extern crate ethereum_types;
extern crate rustc_hex;
extern crate ethcore;

use ethereum_types::{U256, H256};
use rustc_hex::ToHex;

use bridge::bridge::create_withdraw_relay;
use bridge::message_to_mainnet::MessageToMainnet;
use bridge::signature::Signature;
use bridge::contracts;

use std::sync::RwLock;

const COLLECTED_SIGNATURES_TOPIC: &str = "0xeb043d149eedb81369bec43d4c3a3a53087debc88d2525f13bfaa3eecda28b5c";

// 1 signature required. relay polled twice.
// no CollectedSignatures on ForeignBridge.
// no relay.
test_app_stream! {
	name => withdraw_relay_no_log_no_relay,
	database => Database::default(),
	home =>
		account => "0000000000000000000000000000000000000001",
		confirmations => 12;
	foreign =>
		account => "0000000000000000000000000000000000000001",
		confirmations => 12;
	authorities =>
		accounts => [
			"0000000000000000000000000000000000000001",
			"0000000000000000000000000000000000000002",
		],
		signatures => 1;
	txs => Transactions::default(),
	init => |app, db| create_withdraw_relay(app, db, Arc::new(RwLock::new(Some(99999999999u64.into()))), 17, Arc::new(RwLock::new(1))).take(2),
	expected => vec![0x1005, 0x1006],
	home_transport => [],
	foreign_transport => [
		"eth_blockNumber" =>
			req => json!([]),
			res => json!("0x1011");
		"eth_getLogs" =>
			req => json!([{
				"address": ["0x0000000000000000000000000000000000000000"],
				"fromBlock": "0x1",
				"limit": null,
				"toBlock": "0x1005",
				"topics": [[COLLECTED_SIGNATURES_TOPIC], null, null, null]
			}]),
			res => json!([]);
		"eth_blockNumber" =>
			req => json!([]),
			res => json!("0x1012");
		"eth_getLogs" =>
			req => json!([{
				"address": ["0x0000000000000000000000000000000000000000"],
				"fromBlock": "0x1006",
				"limit": null,
				"toBlock": "0x1006",
				"topics": [[COLLECTED_SIGNATURES_TOPIC], null, null, null]
			}]),
			res => json!([]);
	]
}

// 2 signatures required. relay polled twice.
// single CollectedSignatures log present. message value covers relay cost.
// authority not responsible.
// message is ignored.
test_app_stream! {
	name => withdraw_relay_single_log_authority_not_responsible_no_relay,
	database => Database::default(),
	home =>
		account => "0000000000000000000000000000000000000001",
		confirmations => 12;
	foreign =>
		account => "0000000000000000000000000000000000000001",
		confirmations => 12;
	authorities =>
		accounts => [
			"0000000000000000000000000000000000000001",
			"0000000000000000000000000000000000000002",
		],
		signatures => 1;
	txs => Transactions::default(),
	init => |app, db| create_withdraw_relay(app, db, Arc::new(RwLock::new(Some(99999999999u64.into()))), 17, Arc::new(RwLock::new(1))).take(1),
	expected => vec![0x1005],
	home_transport => [],
	foreign_transport => [
		"eth_blockNumber" =>
			req => json!([]),
			res => json!("0x1011");
		"eth_getLogs" =>
			req => json!([{
				"address": ["0x0000000000000000000000000000000000000000"],
				"fromBlock": "0x1",
				"limit": null,
				"toBlock": "0x1005",
				"topics": [[COLLECTED_SIGNATURES_TOPIC], null, null, null]
			}]),
			res => json!([{
				"address": "0x0000000000000000000000000000000000000000",
				"topics": [COLLECTED_SIGNATURES_TOPIC],
				"data": "0x000000000000000000000000aff3454fce5edbc8cca8697c15331677e6ebcccc00000000000000000000000000000000000000000000000000000000000000f0",
				"type": "",
				"transactionHash": "0x884edad9ce6fa2440d8a54cc123490eb96d2768479d49ff9c7366125a9424364"
			}]);
	]
}

// 2 signatures required. relay polled twice.
// single CollectedSignatures log present.
// message gets relayed.
test_app_stream! {
	name => withdraw_relay_single_log_sufficient_value_relay,
	database => Database {
		home_contract_address: "00000000000000000000000000000000000000dd".into(),
		foreign_contract_address: "00000000000000000000000000000000000000ee".into(),
		..Default::default()
	},
	home =>
		account => "0000000000000000000000000000000000000001",
		confirmations => 12;
	foreign =>
		account => "aff3454fce5edbc8cca8697c15331677e6ebcccc",
		confirmations => 12;
	authorities =>
		accounts => [
			"0000000000000000000000000000000000000001",
			"0000000000000000000000000000000000000002",
		],
		signatures => 2;
	txs => Transactions::default(),
	init => |app, db| create_withdraw_relay(app, db, Arc::new(RwLock::new(Some(99999999999u64.into()))), 17, Arc::new(RwLock::new(1))).take(1),
	expected => vec![0x1005],
	home_transport => [
		// `HomeBridge.withdraw`
		"eth_sendTransaction" =>
			req => json!([{
				"data": format!("0x{}", contracts::home::HomeBridge::default()
					.functions()
					.withdraw()
					.input(
						vec![U256::from(1), U256::from(4)],
						vec![H256::from(2), H256::from(5)],
						vec![H256::from(3), H256::from(6)],
						MessageToMainnet {
							recipient: [1u8; 20].into(),
							value: 10000.into(),
							sidenet_transaction_hash: "0x884edad9ce6fa2440d8a54cc123490eb96d2768479d49ff9c7366125a9424364".into(),
							mainnet_gas_price: 1000.into(),
						}.to_bytes()
					).to_hex()),
				"from": "0x0000000000000000000000000000000000000001",
				"gas": "0x0",
				"gasPrice": "0x3e8",
				"to": "0x00000000000000000000000000000000000000dd"
			}]),
			res => json!("0x1db8f385535c0d178b8f40016048f3a3cffee8f94e68978ea4b277f57b638f0b");
	],
	foreign_transport => [
		"eth_blockNumber" =>
			req => json!([]),
			res => json!("0x1011");
		"eth_getLogs" =>
			req => json!([{
				"address": ["0x00000000000000000000000000000000000000ee"],
				"fromBlock": "0x1",
				"limit": null,
				"toBlock": "0x1005",
				"topics": [[COLLECTED_SIGNATURES_TOPIC], null, null, null]
			}]),
			res => json!([{
				"address": "0x00000000000000000000000000000000000000ee",
				"topics": [COLLECTED_SIGNATURES_TOPIC],
				"data": "0x000000000000000000000000aff3454fce5edbc8cca8697c15331677e6ebcccc00000000000000000000000000000000000000000000000000000000000000f0",
				"type": "",
				"transactionHash": "0x884edad9ce6fa2440d8a54cc123490eb96d2768479d49ff9c7366125a9424364"
			}]);
		// call to `message`
		"eth_call" =>
			req => json!([{
				"data": "0x490a32c600000000000000000000000000000000000000000000000000000000000000f0",
				"to": "0x00000000000000000000000000000000000000ee"
			}, "latest"]),
			res => json!(format!("0x{}", MessageToMainnet {
				recipient: [1u8; 20].into(),
				value: 10000.into(),
				sidenet_transaction_hash: "0x884edad9ce6fa2440d8a54cc123490eb96d2768479d49ff9c7366125a9424364".into(),
				mainnet_gas_price: 1000.into(),
			}.to_payload().to_hex()));
		// calls to `signature`
		"eth_call" =>
			req => json!([{
				"data": "0x1812d99600000000000000000000000000000000000000000000000000000000000000f00000000000000000000000000000000000000000000000000000000000000000",
				"to": "0x00000000000000000000000000000000000000ee"
			},"latest"]),
			res => json!(format!("0x{}", Signature { v: 1, r: 2.into(), s: 3.into() }.to_payload().to_hex()));
		"eth_call" =>
			req => json!([{
				"data": "0x1812d99600000000000000000000000000000000000000000000000000000000000000f00000000000000000000000000000000000000000000000000000000000000001",
				"to": "0x00000000000000000000000000000000000000ee"
			},"latest"]),
			res => json!(format!("0x{}", Signature { v: 4, r: 5.into(), s: 6.into() }.to_payload().to_hex()));
	]
}
