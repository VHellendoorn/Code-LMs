package lila.db

import org.joda.time.DateTime
import reactivemongo.api.bson._
import scala.util.{ Failure, Success, Try }
import reactivemongo.api.bson.exceptions.TypeDoesNotMatchException

trait Handlers {

  implicit val BSONJodaDateTimeHandler = quickHandler[DateTime](
    { case v: BSONDateTime => new DateTime(v.value) },
    v => BSONDateTime(v.getMillis)
  )

  def quickHandler[T](read: PartialFunction[BSONValue, T], write: T => BSONValue): BSONHandler[T] =
    new BSONHandler[T] {
      def readTry(bson: BSONValue) =
        read
          .andThen(Success(_))
          .applyOrElse(
            bson,
            (b: BSONValue) => handlerBadType(b)
          )
      def writeTry(t: T) = Success(write(t))
    }

  def tryHandler[T](read: PartialFunction[BSONValue, Try[T]], write: T => BSONValue): BSONHandler[T] =
    new BSONHandler[T] {
      def readTry(bson: BSONValue) =
        read.applyOrElse(
          bson,
          (b: BSONValue) => handlerBadType(b)
        )
      def writeTry(t: T) = Success(write(t))
    }

  def handlerBadType[T](b: BSONValue): Try[T] =
    Failure(TypeDoesNotMatchException("BSONValue", b.getClass.getSimpleName))

  def handlerBadValue[T](msg: String): Try[T] =
    Failure(new IllegalArgumentException(msg))

  def stringMapHandler[V](implicit
      reader: BSONReader[Map[String, V]],
      writer: BSONWriter[Map[String, V]]
  ) =
    new BSONHandler[Map[String, V]] {
      def readTry(bson: BSONValue)    = reader readTry bson
      def writeTry(v: Map[String, V]) = writer writeTry v
    }

  implicit val colorBoolHandler = BSONBooleanHandler.as[chess.Color](chess.Color.fromWhite, _.white)
}
