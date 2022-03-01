// Mock bitcoin-rpc interface
use bitcoincore_rpc::Error;
extern crate hex;

pub struct MockBitcoinClient {}

impl MockBitcoinClient {
    pub fn new() -> MockBitcoinClient {
        MockBitcoinClient {}
    }
    pub fn get_block_count(&mut self) -> std::result::Result<u64,()> {
        return Ok(147 as u64)
     }
    pub fn send_raw_transaction(&mut self, raw_tx: &std::vec::Vec<u8>) -> Result<String,Error> {
        let backup_tx_1: Vec::<u8> = hex::decode("02000000000101b91e2b8e26ae7f93cea773c5d74f7722982134ebbf32ca9b627981a5546ef4c7000000001716001472d64fcb0be3dff555fc87b3d054a1ccb48ac059feffffff0200c2eb0b0000000017a9141040c0c1b81e2e00aec47ef01c2d3a6116ca513d8748b723180100000017a914d5dd335a7721cf03b1f5df5bdf22c63c0e1e472887024730440220167f84b7e579153ff83a480eadc4225ad1c67322ad0e8d5f32d317ce61a6c26802206fff7f176b6780f00cf9d63ea759658e4ca0302dc2204c02bf3ee52e032e051001210297fd944ebb0de31b629a99a14d53fb8c83e5791f714892f72b74751cfd097c1765000000").unwrap();
        let backup_tx_2: Vec::<u8> = hex::decode("020000000001010a742dc732ef1ea6a71c042b7fa212457b52438ba5c3b8552b8a4fd74e86a0f601000000171600147a91e5a412a6a826897067654fffb1557741285efeffffff0240860f240100000017a9140dbb4870526bb96a42ebe19dc86d84a34addc5d48700e1f5050000000017a9141040c0c1b81e2e00aec47ef01c2d3a6116ca513d8702483045022100e7d13322ee719ae8fb7775cafec98137d8d3c42e340cda7750679a06308744f602206154f097a7bc625c688db343633cdafa9e48455d90630d21edf8e036faa0ddbf0121034ea2ae3c24aea00b262c557675d82b66d9aa0f2bc14dfa7d82d1983efb0456c984000000").unwrap();

        if raw_tx == &backup_tx_1 {
            return Ok("e3d514ad83995f5c3407a3a6317355fe22d3b24ab2e89455f9db504e3bfb3c86".to_string());
        }
        else if raw_tx == &backup_tx_2 {
            return Ok("988d8d8de7a81d859c90d0a6b3b577d622608bbe22ce861f71eb46aa662696fb".to_string());
        }
        Ok("".to_string())
    }
}
