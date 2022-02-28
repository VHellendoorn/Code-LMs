package org.scalajs.nio

import scala.scalajs.js
import js.Dynamic.{ global => g }
import org.scalajs.dom

class AdaptiveShortBuffer(cap: Int, lim: Int, pos: Int, mar: Int, mBuffer: dom.ArrayBuffer,
  mBufferOffset: Int, mByteOrder: ByteOrder) extends NativeShortBuffer(cap, lim, pos, mar, mBuffer, mBufferOffset) {
  protected val littleEndian: Boolean = mByteOrder == LittleEndian

  override protected def iGet(index: Int): Short = {
    this.dataView.getInt16(index * this.bytes_per_element, this.littleEndian).toShort
  }
  override protected def iSet(index: Int, value: Short): Unit = {
    this.dataView.setInt16(index * this.bytes_per_element, value, this.littleEndian)
  }

  override def duplicate(): ShortBuffer = {
    new AdaptiveShortBuffer(this.mCapacity, this.mLimit, this.mPosition, this.mMark,
      this.mBuffer, this.mBufferOffset, mByteOrder)
  }
  override def slice(): ShortBuffer = {
    new AdaptiveShortBuffer(this.remaining, this.remaining, 0, -1, this.mBuffer,
      this.mBufferOffset + (this.mPosition * this.bytes_per_element), mByteOrder)
  }
  override def asReadOnlyBuffer(): ShortBuffer = {
    new ReadOnlyShortBuffer(this.duplicate)
  }
  override def order(): ByteOrder = {
    if (littleEndian)
      LittleEndian
    else
      BigEndian
  }

  override val hasJsArray = order() == ByteOrder.nativeOrder
  override def jsArray(): dom.Int16Array = if (!hasJsArray) throw new UnsupportedOperationException else super.jsArray

  override def toString = "AdaptiveShortBuffer[pos=" + this.position + " lim=" + this.limit + " cap=" + this.capacity + "]"
}

object AdaptiveShortBuffer {
  def allocate(capacity: Int): NativeShortBuffer = this.allocate(capacity, ByteOrder.nativeOrder)
  def allocate(capacity: Int, byteOrder: ByteOrder): NativeShortBuffer = {
    if (byteOrder == ByteOrder.nativeOrder) {
      NativeShortBuffer.allocate(capacity)
    } else {
      val jsBuffer = js.Dynamic.newInstance(g.ArrayBuffer)(capacity * NativeShortBuffer.BYTES_PER_ELEMENT).asInstanceOf[dom.ArrayBuffer]
      val shortBuffer = new AdaptiveShortBuffer(capacity, capacity, 0, -1, jsBuffer, 0, byteOrder)
      shortBuffer
    }
  }

  def wrap(array: Array[Short]): NativeShortBuffer = this.wrap(array, ByteOrder.nativeOrder)
  def wrap(array: Array[Short], byteOrder: ByteOrder): NativeShortBuffer = wrap(array, 0, array.length, byteOrder)
  def wrap(array: Array[Short], offset: Int, length: Int): NativeShortBuffer = this.wrap(array, offset, length, ByteOrder.nativeOrder)
  def wrap(array: Array[Short], offset: Int, length: Int, byteOrder: ByteOrder): NativeShortBuffer = {
    val shortBuffer = this.allocate(length, byteOrder)
    var i = 0
    while (i < length) {
      shortBuffer.put(i, array(i + offset))
      i += 1
    }
    shortBuffer
  }
}