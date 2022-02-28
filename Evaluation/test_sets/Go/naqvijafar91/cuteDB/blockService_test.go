package cutedb

import (
	"os"
	"testing"
)

func initBlockService() *blockService {
	path := "./db/test.db"
	if _, err := os.Stat(path); os.IsNotExist(err) {
		os.Mkdir("./db", os.ModePerm)
	}
	if _, err := os.Stat(path); err == nil {
		// path/to/whatever exists
		err := os.Remove(path)
		if err != nil {
			panic(err)
		}
	}
	file, err := os.OpenFile(path, os.O_RDWR|os.O_CREATE, 0666)
	if err != nil {
		panic(err)
	}
	return newBlockService(file)
}

func TestShouldGetNegativeIfBlockNotPresent(t *testing.T) {
	blockService := initBlockService()
	latestBlockID, _ := blockService.getLatestBlockID()
	if latestBlockID != -1 {
		t.Error("Should get negative block id")
	}
}

func TestShouldSuccessfullyInitializeNewBlock(t *testing.T) {
	blockService := initBlockService()
	block, err := blockService.getRootBlock()
	if err != nil {
		t.Error(err)
	}
	if block.id != 0 {
		t.Error("Root Block id should be zero")
	}

	if block.currentLeafSize != 0 {
		t.Error("Block leaf size should be zero")
	}
}

func TestShouldSaveNewBlockOnDisk(t *testing.T) {
	blockService := initBlockService()
	block, err := blockService.getRootBlock()
	if err != nil {
		t.Error(err)
	}
	if block.id != 0 {
		t.Error("Root Block id should be zero")
	}

	if block.currentLeafSize != 0 {
		t.Error("Block leaf size should be zero")
	}
	elements := make([]*pairs, 3)
	elements[0] = newPair("hola", "amigos")
	elements[1] = newPair("foo", "bar")
	elements[2] = newPair("gooz", "bumps")
	block.setData(elements)
	err = blockService.writeBlockToDisk(block)
	if err != nil {
		t.Error(err)
	}

	block, err = blockService.getRootBlock()
	if err != nil {
		t.Error(err)
	}

	if len(block.dataSet) == 0 {
		t.Error("Length of data field should not be zero")
	}
}

func TestShouldConvertPairToAndFromBytes(t *testing.T) {
	pair := &pairs{}
	pair.setKey("Hola  ")
	pair.setValue("Amigos")
	pairBytes := convertPairsToBytes(pair)
	convertedPair := convertBytesToPair(pairBytes)

	if pair.keyLen != convertedPair.keyLen || pair.valueLen != convertedPair.valueLen {
		t.Error("Lengths do not match")
	}

	if pair.key != convertedPair.key || pair.value != convertedPair.value {
		t.Error("Values do not match")
	}
}

func TestShouldConvertBlockToAndFromBytes(t *testing.T) {
	blockService := initBlockService()
	block := &diskBlock{}
	block.setChildren([]uint64{2, 3, 4, 6})

	elements := make([]*pairs, 3)
	elements[0] = newPair("hola", "amigos")
	elements[1] = newPair("foo", "bar")
	elements[2] = newPair("gooz", "bumps")
	block.setData(elements)
	blockBuffer := blockService.getBufferFromBlock(block)
	convertedBlock := blockService.getBlockFromBuffer(blockBuffer)

	if convertedBlock.childrenBlockIds[2] != 4 {
		t.Error("Should contain 4 at 2nd index")
	}

	if len(convertedBlock.dataSet) != len(block.dataSet) {
		t.Error("Length of blocks should be same")
	}

	if convertedBlock.dataSet[1].key != block.dataSet[1].key {
		t.Error("Keys dont match")
	}

	if convertedBlock.dataSet[2].value != block.dataSet[2].value {
		t.Error("Values dont match")
	}
}

func TestShouldConvertToAndFromDiskNode(t *testing.T) {
	bs := initBlockService()
	node := &DiskNode{}
	node.blockID = 55
	elements := make([]*pairs, 3)
	elements[0] = newPair("hola", "amigos")
	node.keys = elements
	node.childrenBlockIDs = []uint64{1000, 10001}
	block := bs.convertDiskNodeToBlock(node)

	if block.id != 55 {
		t.Error("Should have same block id as node block id")
	}
	if block.childrenBlockIds[1] != 10001 {
		t.Error("Block ids should match")
	}

	nodeFromBlock := bs.convertBlockToDiskNode(block)

	if nodeFromBlock.blockID != node.blockID {
		t.Error("Block ids should match")
	}

	if nodeFromBlock.childrenBlockIDs[0] != 1000 {
		t.Error("Child Block ids should match")
	}
	if nodeFromBlock.keys[0].key != "hola" {
		t.Error("Data elements should match")
	}
}
