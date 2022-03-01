// Electrum server http RESTful API interface

use bitcoin::consensus;
use bitcoin::Transaction;
use electrumx_client::interface::Electrumx;
use electrumx_client::response::{
    GetBalanceResponse, GetBlockHeadersResponse, GetHistoryResponse, GetListUnspentResponse,
    GetTipResponse, GetTransactionConfStatus,
};

pub struct MockElectrum {}

impl MockElectrum {
    pub fn new() -> MockElectrum {
        MockElectrum {}
    }
}

impl Electrumx for MockElectrum {
    fn get_tip_header(
        &mut self,
    ) -> std::result::Result<GetTipResponse, Box<dyn std::error::Error>> {
        return Ok(GetTipResponse {
            height: 12345,
            hex: "AA".to_string(),
        });
    }
    fn get_block_header(&mut self, _height: usize) -> Result<String, Box<dyn std::error::Error>> {
        todo!()
    }
    fn get_block_headers(
        &mut self,
        _start_height: usize,
        _count: usize,
    ) -> Result<GetBlockHeadersResponse, Box<dyn std::error::Error>> {
        todo!()
    }
    fn estimate_fee(&mut self, _number: usize) -> Result<f64, Box<dyn std::error::Error>> {
        todo!()
    }
    fn relay_fee(&mut self) -> Result<f64, Box<dyn std::error::Error>> {
        todo!()
    }
    fn get_history(
        &mut self,
        _addr: &str,
    ) -> Result<Vec<GetHistoryResponse>, Box<dyn std::error::Error>> {
        todo!()
    }
    fn get_mempool(&mut self, _addr: &str) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
        todo!()
    }
    fn history(&mut self, _addr: &str) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
        todo!()
    }
    fn get_utxos(&mut self, _addr: &str) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
        todo!()
    }
    fn broadcast_transaction(
        &mut self,
        raw_tx: String,
    ) -> Result<String, Box<dyn std::error::Error>> {
        let tx: Transaction = consensus::deserialize(&hex::decode(&raw_tx).unwrap()).unwrap();
        Ok(tx.txid().to_string())
    }
    fn get_transaction(
        &mut self,
        _tx_hash: String,
        _merkle: bool,
    ) -> Result<String, Box<dyn std::error::Error>> {
        Ok("02000000000103c8e94ea0f9b1354a2dcbbf5d29090b60f89430d953d36c8631010ca436ecf80e0000000000fdffffff73e21c3ccb1cdfe3814701a6a9a4fda6547a7d8a37f2dc75f78f754a820457620000000000fdffffff79206059f932f665953bd5ebcf5f45624f346d094a483356257755f9951796d80000000000fdffffff02a00901000000000016001427628f1e9ce4e02353e7984ade02c6cb118a4218a08601000000000016001458b0d7ba80de7e0d06502eb1264d878da268636402473044022056f51aed4b34dec63c0a1de18ef78f6f9dbb455309d0e0719ad3cfdc33b40eea022063c035562b24670a1a27edc53b984743c35de9ed6af1d5e4fe16bdd437dc6cb70121029a5d481ea12bd4402db427cbdd475c535c3293cec2abf2c01852265787dd41f402463043021f05e0cf5935c9d4a2ab4ed04518f5dc73a4e39f5989e0d183746c92e3139264022009b3b20c9c6ffa46773eacf6c1803840c9843e0b3e774bec57ffb8d5fd1091f4012103218ba8362f04bc3a665fdfc2708b13e09e7a989f112f42a71bb4eaf86338ffbf0247304402200b1f9566ff1f52ad88d7f2a25d988dba659ec8ba2cac4a7f820bf6db555ed04902205ba96f2a23a286cb21dd46356debe1b03a32e12e4ab268cdd556f23ff939bdbd01210257e3c32610c41cd1cec0d0c07c484be9307e8e1585920a23a00aa2610744b7d9bc222000".to_string())
    }
    fn get_transaction_conf_status(
        &mut self,
        _tx_hash: String,
        _merkle: bool,
    ) -> Result<GetTransactionConfStatus, Box<dyn std::error::Error>> {
        Ok(GetTransactionConfStatus {
            in_active_chain: Some(true),
            confirmations: Some(3),
            blocktime: Some(123456789),
        })
    }
    fn get_merkle_transaction(
        &mut self,
        _tx_hash: String,
        _height: usize,
    ) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
        todo!()
    }
    fn transaction_id_from_pos(
        &mut self,
        _height: usize,
        _tx_pos: usize,
        _merkle: bool,
    ) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
        todo!()
    }
    fn get_fee_histogram_mempool(&mut self) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
        todo!()
    }
    fn get_balance(
        &mut self,
        addr: &str,
    ) -> Result<GetBalanceResponse, Box<dyn std::error::Error>> {
        if addr == "bcrt1qghtup486tj8vgz2l5pkh8hqw8wzdudraa6hnzs" {
            return Ok(GetBalanceResponse {
                unconfirmed: 0,
                confirmed: 100,
            });
        }
        if addr == "tb1qghtup486tj8vgz2l5pkh8hqw8wzdudralnw74e" {
            return Ok(GetBalanceResponse {
                unconfirmed: 0,
                confirmed: 100,
            });
        }
        if addr == "bcrt1qsuqsurhgfduhqw6ejquw54482sqpkfc22gytyh" {
            return Ok(GetBalanceResponse {
                unconfirmed: 0,
                confirmed: 10000000,
            });
        }
        if addr == "tb1qsuqsurhgfduhqw6ejquw54482sqpkfc2gpaxn7" {
            return Ok(GetBalanceResponse {
                unconfirmed: 0,
                confirmed: 10000000,
            });
        }
        Ok(GetBalanceResponse {
            unconfirmed: 0,
            confirmed: 0,
        })
    }
    fn get_list_unspent(
        &mut self,
        addr: &str,
    ) -> Result<Vec<GetListUnspentResponse>, Box<dyn std::error::Error>> {
        if addr == "bcrt1qghtup486tj8vgz2l5pkh8hqw8wzdudraa6hnzs" {
            return Ok(vec![GetListUnspentResponse {
                height: 123,
                tx_hash: "e0a97cb38e7e73617ef75a57eaf2841eb06833407c0eae08029bd04ea7e6115a"
                    .to_string(),
                tx_pos: 0,
                value: 100,
            }]);
        }
        if addr == "tb1qghtup486tj8vgz2l5pkh8hqw8wzdudralnw74e" {
            return Ok(vec![GetListUnspentResponse {
                height: 123,
                tx_hash: "e0a97cb38e7e73617ef75a57eaf2841eb06833407c0eae08029bd04ea7e6115a"
                    .to_string(),
                tx_pos: 0,
                value: 100,
            }]);
        }
        if addr == "bcrt1qsuqsurhgfduhqw6ejquw54482sqpkfc22gytyh" {
            return Ok(vec![GetListUnspentResponse {
                height: 1234,
                tx_hash: "40bf39ffdf4322e4d30ed783feec5bd9eb2804b81f23ebd5e24ea2aa2365a326"
                    .to_string(),
                tx_pos: 1,
                value: 10000000,
            }]);
        }
        if addr == "tb1qsuqsurhgfduhqw6ejquw54482sqpkfc2gpaxn7" {
            return Ok(vec![GetListUnspentResponse {
                height: 1234,
                tx_hash: "40bf39ffdf4322e4d30ed783feec5bd9eb2804b81f23ebd5e24ea2aa2365a326"
                    .to_string(),
                tx_pos: 1,
                value: 10000000,
            }]);
        }
        Ok(vec![])
    }
}
