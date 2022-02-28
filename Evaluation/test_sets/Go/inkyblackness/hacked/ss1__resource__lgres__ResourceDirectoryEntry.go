package lgres

import (
	"math"
)

type resourceDirectoryEntry struct {
	ID                            uint16
	UnpackedLengthAndResourceType uint32
	PackedLengthAndContentType    uint32
}

func maskBits(field uint32, bitOffset uint, bitCount int) uint32 {
	return (field >> bitOffset) & uint32(^(uint64(math.MaxUint64) << uint64(bitCount)))
}

func setBits(field uint32, bitOffset uint, bitCount int, value uint32) uint32 {
	mask := uint32(^(uint64(math.MaxUint64) << uint64(bitCount)))
	return (field & (^mask << bitOffset)) | ((value & mask) << bitOffset)
}

func (entry *resourceDirectoryEntry) setUnpackedLength(value uint32) {
	entry.UnpackedLengthAndResourceType = setBits(entry.UnpackedLengthAndResourceType, 0, 24, value)
}

func (entry *resourceDirectoryEntry) unpackedLength() uint32 {
	return maskBits(entry.UnpackedLengthAndResourceType, 0, 24)
}

func (entry *resourceDirectoryEntry) setResourceType(value byte) {
	entry.UnpackedLengthAndResourceType = setBits(entry.UnpackedLengthAndResourceType, 24, 8, uint32(value))
}

func (entry *resourceDirectoryEntry) resourceType() byte {
	return byte(maskBits(entry.UnpackedLengthAndResourceType, 24, 8))
}

func (entry *resourceDirectoryEntry) setPackedLength(value uint32) {
	entry.PackedLengthAndContentType = setBits(entry.PackedLengthAndContentType, 0, 24, value)
}

func (entry *resourceDirectoryEntry) packedLength() uint32 {
	return maskBits(entry.PackedLengthAndContentType, 0, 24)
}

func (entry *resourceDirectoryEntry) setContentType(value byte) {
	entry.PackedLengthAndContentType = setBits(entry.PackedLengthAndContentType, 24, 8, uint32(value))
}

func (entry *resourceDirectoryEntry) contentType() byte {
	return byte(maskBits(entry.PackedLengthAndContentType, 24, 8))
}
