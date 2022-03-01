use web3::types::{H256, Address, FilterBuilder};
use ethabi;

fn web3_topic(topic: ethabi::Topic<ethabi::Hash>) -> Option<Vec<H256>> {
	let t: Vec<ethabi::Hash> = topic.into();
	// parity does not conform to an ethereum spec
	if t.is_empty() {
		None
	} else {
		Some(t)
	}
}

pub fn web3_filter<I: IntoIterator<Item = Address>>(filter: ethabi::TopicFilter, addresses: I) -> FilterBuilder {
	let t0 = web3_topic(filter.topic0);
	let t1 = web3_topic(filter.topic1);
	let t2 = web3_topic(filter.topic2);
	let t3 = web3_topic(filter.topic3);
	FilterBuilder::default()
		.address(addresses.into_iter().collect())
		.topics(t0, t1, t2, t3)
}
