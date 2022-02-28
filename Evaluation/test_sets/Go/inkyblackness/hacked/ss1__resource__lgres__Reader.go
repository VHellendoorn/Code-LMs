package lgres

import (
	"bytes"
	"encoding/binary"
	"io"
	"io/ioutil"

	"github.com/inkyblackness/hacked/ss1"
	"github.com/inkyblackness/hacked/ss1/resource"
	"github.com/inkyblackness/hacked/ss1/resource/lgres/internal/compression"
	"github.com/inkyblackness/hacked/ss1/resource/lgres/internal/format"
	"github.com/inkyblackness/hacked/ss1/serial"
)

// Reader provides methods to extract resource data from a serialized form.
// Resources may be accessed out of sequence due to the nature of the underlying io.ReaderAt.
type Reader struct {
	source              io.ReaderAt
	firstResourceOffset uint32
	directoryOffset     uint32
	directory           []resourceDirectoryEntry

	cache map[uint16]resource.View
}

const (
	// ErrSourceNil is returned if the source is not valid.
	ErrSourceNil ss1.StringError = "source is nil"

	// ErrFormatMismatch is returned if the format is unknown.
	ErrFormatMismatch ss1.StringError = "format mismatch"
)

// ReaderFrom accesses the provided source and creates a new Reader instance
// from it.
// Should the provided decoder not follow the resource file format, an error
// is returned.
func ReaderFrom(source io.ReaderAt) (reader *Reader, err error) {
	if source == nil {
		return nil, ErrSourceNil
	}

	var dirOffset uint32
	dirOffset, err = readAndVerifyHeader(io.NewSectionReader(source, 0, format.ResourceDirectoryFileOffsetPos+4))
	if err != nil {
		return nil, err
	}
	firstResourceOffset, directory, err := readDirectoryAt(dirOffset, source)
	if err != nil {
		return nil, err
	}

	reader = &Reader{
		source:              source,
		firstResourceOffset: firstResourceOffset,
		directoryOffset:     dirOffset,
		directory:           directory,
		cache:               make(map[uint16]resource.View)}

	return
}

// IDs returns the resource identifier available via this reader.
// The order in the slice is the same as in the underlying serialized form.
func (reader *Reader) IDs() []resource.ID {
	ids := make([]resource.ID, len(reader.directory))
	for index, entry := range reader.directory {
		ids[index] = resource.ID(entry.ID)
	}
	return ids
}

// View returns a reader prepared to extract data for the specified resource.
// An error is returned if either the ID is not known, or the resource could not be prepared.
func (reader *Reader) View(id resource.ID) (retrievedResource resource.View, err error) {
	if cachedResource, existing := reader.cache[id.Value()]; existing {
		return cachedResource, nil
	}
	resourceStartOffset, entry := reader.findEntry(id.Value())
	if entry == nil {
		return nil, resource.ErrNotFound(id)
	}
	resourceType := entry.resourceType()
	compressed := (resourceType & format.ResourceTypeFlagCompressed) != 0
	compound := (resourceType & format.ResourceTypeFlagCompound) != 0
	contentType := resource.ContentType(entry.contentType())

	if compound {
		retrievedResource, err = reader.newCompoundResourceReader(entry, contentType, compressed, resourceStartOffset)
	} else {
		retrievedResource, err = reader.newSingleBlockResourceReader(entry, contentType, compressed, resourceStartOffset)
	}
	if err == nil {
		reader.cache[id.Value()] = retrievedResource
	}
	return
}

func readAndVerifyHeader(source io.ReadSeeker) (dirOffset uint32, err error) {
	coder := serial.NewPositioningDecoder(source)
	data := make([]byte, format.ResourceDirectoryFileOffsetPos)
	coder.Code(data)
	coder.Code(&dirOffset)

	expected := make([]byte, len(format.HeaderString)+1)
	for index, r := range format.HeaderString {
		expected[index] = byte(r)
	}
	expected[len(format.HeaderString)] = format.CommentTerminator
	if !bytes.Equal(data[:len(expected)], expected) {
		return 0, ErrFormatMismatch
	}

	return dirOffset, coder.FirstError()
}

func readDirectoryAt(dirOffset uint32, source io.ReaderAt) (firstResourceOffset uint32, directory []resourceDirectoryEntry, err error) {
	var header resourceDirectoryHeader
	headerSize := int64(binary.Size(&header))
	{
		headerCoder := serial.NewDecoder(io.NewSectionReader(source, int64(dirOffset), headerSize))
		headerCoder.Code(&header)
		if headerCoder.FirstError() != nil {
			return 0, nil, headerCoder.FirstError()
		}
	}

	firstResourceOffset = header.FirstResourceOffset
	directory = make([]resourceDirectoryEntry, header.ResourceCount)
	if header.ResourceCount > 0 {
		listCoder := serial.NewDecoder(io.NewSectionReader(source, int64(dirOffset)+headerSize, int64(binary.Size(directory))))
		listCoder.Code(directory)
		err = listCoder.FirstError()
	}
	return
}

func (reader *Reader) findEntry(id uint16) (startOffset uint32, entry *resourceDirectoryEntry) {
	startOffset = reader.firstResourceOffset
	for index := 0; (index < len(reader.directory)) && (entry == nil); index++ {
		cur := &reader.directory[index]
		if cur.ID == id {
			entry = cur
		} else {
			startOffset += cur.packedLength()
			startOffset += (format.BoundarySize - (startOffset % format.BoundarySize)) % format.BoundarySize
		}
	}
	return
}

type blockListEntry struct {
	start uint32
	size  uint32
}

func (reader *Reader) newCompoundResourceReader(entry *resourceDirectoryEntry,
	contentType resource.ContentType, compressed bool, resourceStartOffset uint32) (resource.View, error) {
	resourceDataReader := io.NewSectionReader(reader.source, int64(resourceStartOffset), int64(entry.packedLength()))

	firstBlockOffset, blockList, err := reader.readBlockList(resourceDataReader)
	if err != nil {
		return nil, err
	}
	blockCount := len(blockList)

	rawBlockDataReader := io.NewSectionReader(resourceDataReader, int64(firstBlockOffset), resourceDataReader.Size()-int64(firstBlockOffset))
	var uncompressedReader io.ReaderAt
	if !compressed {
		uncompressedReader = rawBlockDataReader
	}

	blockFunc := func(index int) (io.Reader, error) {
		if (index < 0) || (index >= blockCount) {
			return nil, resource.ErrBlockNotFound(index, blockCount)
		}

		if compressed && (uncompressedReader == nil) {
			decompressor := compression.NewDecompressor(rawBlockDataReader)
			decompressedData, err := ioutil.ReadAll(decompressor)
			if err != nil {
				return nil, err
			}
			uncompressedReader = bytes.NewReader(decompressedData)
		}

		entry := blockList[index]
		reader := io.NewSectionReader(uncompressedReader, int64(entry.start)-int64(firstBlockOffset), int64(entry.size))
		return reader, nil
	}

	return &readerResource{
		Properties: resource.Properties{
			Compound:    true,
			ContentType: contentType,
			Compressed:  compressed,
		},
		blockReader: blockReader{blockCount: len(blockList), blockFunc: blockFunc}}, nil
}

func (reader *Reader) readBlockList(source io.Reader) (uint32, []blockListEntry, error) {
	listDecoder := serial.NewDecoder(source)
	var blockCount uint16
	listDecoder.Code(&blockCount)
	var firstBlockOffset uint32
	listDecoder.Code(&firstBlockOffset)
	lastBlockEndOffset := firstBlockOffset
	blockList := make([]blockListEntry, blockCount)
	for blockIndex := uint16(0); blockIndex < blockCount; blockIndex++ {
		var endOffset uint32
		listDecoder.Code(&endOffset)
		blockList[blockIndex].start = lastBlockEndOffset
		blockList[blockIndex].size = endOffset - lastBlockEndOffset
		lastBlockEndOffset = endOffset
	}

	return firstBlockOffset, blockList, listDecoder.FirstError()
}

func (reader *Reader) newSingleBlockResourceReader(entry *resourceDirectoryEntry,
	contentType resource.ContentType, compressed bool, resourceStartOffset uint32) (resource.View, error) {
	blockFunc := func(index int) (io.Reader, error) {
		if index != 0 {
			return nil, resource.ErrBlockNotFound(index, 1)
		}
		resourceSize := entry.packedLength()
		// The following check is a hack to re-create the behaviour of the engine.
		// While in truth the resource should only cover what its entry needs, Movie resources
		// can get larger than the 24bit size-field would allow (16MiB).
		// Interestingly, the engine does not limit this and simply reads beyond the limit.
		// In order to support such "broken" files, the behaviour is re-created here. This is mainly to
		// still allow creating such files and also give the editor a chance to work (and display such a case)
		// without completely having to rework the whole architecture here.
		// As audio of logs is also stored with this content type, limit to only single-resource files
		// to avoid a sudden increase of other resources in the file.
		isCutscene := (resource.ContentType(entry.contentType()) == resource.Movie) && (len(reader.directory) == 1)
		if isCutscene && (resourceStartOffset < reader.directoryOffset) {
			resourceSize = reader.directoryOffset - resourceStartOffset
		}
		var resourceSource io.Reader = io.NewSectionReader(reader.source, int64(resourceStartOffset), int64(resourceSize))
		if compressed {
			resourceSize = entry.unpackedLength()
			resourceSource = compression.NewDecompressor(resourceSource)
		}
		return io.LimitReader(resourceSource, int64(resourceSize)), nil
	}

	return &readerResource{
		Properties: resource.Properties{
			Compound:    false,
			ContentType: contentType,
			Compressed:  compressed,
		},
		blockReader: blockReader{blockCount: 1, blockFunc: blockFunc}}, nil
}
