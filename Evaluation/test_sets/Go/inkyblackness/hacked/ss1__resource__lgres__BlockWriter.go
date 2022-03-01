package lgres

import "io"

// BlockWriter is for writing data of a single block in a resource.
type BlockWriter struct {
	target       io.Writer
	finisher     func()
	bytesWritten uint32
}

func (writer *BlockWriter) finish() (length uint32) {
	writer.finisher()
	writer.finisher = nil
	writer.target = nil
	return writer.bytesWritten
}

// Write stores the given data in the block and follows the Writer interface.
func (writer *BlockWriter) Write(data []byte) (written int, err error) {
	written, err = writer.target.Write(data)
	writer.bytesWritten += uint32(written)
	return
}
