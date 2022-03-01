package io.iohk.iodb

import java.io.Serializable
import java.nio.ByteBuffer
import java.util

object ByteArrayWrapper {

  def fromLong(id: Long): ByteArrayWrapper = {
    val b = ByteBuffer.allocate(8)
    b.putLong(0, id)
    ByteArrayWrapper(b.array())
  }
}

/**
  * Wraps byte array and provides hashCode, equals and compare methods.
  */
case class ByteArrayWrapper(data: Array[Byte])
  extends Serializable
    with Comparable[ByteArrayWrapper]
    with Ordered[ByteArrayWrapper] {

  /** alternative constructor which takes array size and creates new empty array */
  def this(size:Int) = this(new Array[Byte](size))

  def size = data.length

  require(data != null)

  //TODO wrapped data immutable?

  override def equals(o: Any): Boolean =
    o.isInstanceOf[ByteArrayWrapper] &&
      util.Arrays.equals(data, o.asInstanceOf[ByteArrayWrapper].data)

  override def hashCode: Int = Utils.byteArrayHashCode(data)

  override def compareTo(o: ByteArrayWrapper): Int = Utils.BYTE_ARRAY_COMPARATOR.compare(this.data, o.data)

  override def compare(that: ByteArrayWrapper): Int = compareTo(that)

  override def toString: String = {
    val v = if (size == 8) {
      //if size is 8, display as a number
      Utils.getLong(data, 0).toString + "L"
    } else {
      javax.xml.bind.DatatypeConverter.printHexBinary(data)
    }
    getClass.getSimpleName + "[" + v + "]"
  }
}