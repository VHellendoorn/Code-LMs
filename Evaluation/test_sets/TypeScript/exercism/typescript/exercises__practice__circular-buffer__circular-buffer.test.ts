import CircularBuffer, {
  BufferFullError,
  BufferEmptyError,
} from './circular-buffer'

describe('CircularBuffer', () => {
  it('reading an empty buffer throws a BufferEmptyError', () => {
    const buffer = new CircularBuffer<string>(1)
    expect(() => buffer.read()).toThrow(BufferEmptyError)
  })

  it('write and read back one item', () => {
    const buffer = new CircularBuffer<string>(1)
    buffer.write('1')
    expect(buffer.read()).toBe('1')
    expect(() => buffer.read()).toThrow(BufferEmptyError)
  })

  it('write and read back multiple items', () => {
    const buffer = new CircularBuffer<string>(2)
    buffer.write('1')
    buffer.write('2')
    expect(buffer.read()).toBe('1')
    expect(buffer.read()).toBe('2')
    expect(() => buffer.read()).toThrow(BufferEmptyError)
  })

  it('clearing a buffer', () => {
    const buffer = new CircularBuffer<string>(2)
    buffer.write('1')
    buffer.write('2')
    buffer.clear()
    expect(() => buffer.read()).toThrow(BufferEmptyError)
    buffer.write('3')
    buffer.write('4')
    expect(buffer.read()).toBe('3')
    expect(buffer.read()).toBe('4')
  })

  it('alternate write and read', () => {
    const buffer = new CircularBuffer<string>(2)
    buffer.write('1')
    expect(buffer.read()).toBe('1')
    buffer.write('2')
    expect(buffer.read()).toBe('2')
  })

  it('reads back oldest item', () => {
    const buffer = new CircularBuffer<string>(3)
    buffer.write('1')
    buffer.write('2')
    buffer.read()
    buffer.write('3')
    expect(buffer.read()).toBe('2')
    expect(buffer.read()).toBe('3')
  })

  it('writing to a full buffer throws a BufferFullError', () => {
    const buffer = new CircularBuffer<string>(2)
    buffer.write('1')
    buffer.write('2')
    expect(() => buffer.write('A')).toThrow(BufferFullError)
  })

  it('forced writes over write oldest item in a full buffer', () => {
    const buffer = new CircularBuffer<string>(2)
    buffer.write('1')
    buffer.write('2')
    buffer.forceWrite('A')
    expect(buffer.read()).toBe('2')
    expect(buffer.read()).toBe('A')
    expect(() => buffer.read()).toThrow(BufferEmptyError)
  })

  it('forced writes act like write in a non-full buffer', () => {
    const buffer = new CircularBuffer<string>(2)
    buffer.write('1')
    buffer.forceWrite('2')
    expect(buffer.read()).toBe('1')
    expect(buffer.read()).toBe('2')
    expect(() => buffer.read()).toThrow(BufferEmptyError)
  })

  it('alternate force write and read into full buffer', () => {
    const buffer = new CircularBuffer<string>(5)
    buffer.write('1')
    buffer.write('2')
    buffer.write('3')
    buffer.read()
    buffer.read()
    buffer.write('4')
    buffer.read()
    buffer.write('5')
    buffer.write('6')
    buffer.write('7')
    buffer.write('8')
    buffer.forceWrite('A')
    buffer.forceWrite('B')
    expect(buffer.read()).toBe('6')
    expect(buffer.read()).toBe('7')
    expect(buffer.read()).toBe('8')
    expect(buffer.read()).toBe('A')
    expect(buffer.read()).toBe('B')
    expect(() => buffer.read()).toThrow(BufferEmptyError)
  })
})
