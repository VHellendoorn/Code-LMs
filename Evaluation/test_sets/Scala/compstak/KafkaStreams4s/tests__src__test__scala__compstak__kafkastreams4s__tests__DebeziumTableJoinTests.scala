package compstak.kafkastreams4s.tests

import org.apache.kafka.streams.StreamsBuilder
import compstak.kafkastreams4s.debezium.DebeziumTable
import io.circe.Encoder
import io.circe.Decoder
import compstak.kafkastreams4s.debezium.DebeziumCompositeType
import cats.effect.Resource
import cats.effect.IO
import cats.implicits._
import compstak.kafkastreams4s.testing.KafkaStreamsTestRunner
import compstak.kafkastreams4s.circe.CirceCodec
import compstak.circe.debezium._
import io.circe.JsonObject

import io.circe.Json
import cats.effect.unsafe.implicits.global

class DebeziumTableJoinTests extends munit.FunSuite {
  test("STable joinOption should work as expected") {
    val out = "out"

    case class A(foo: Int, bar: String)
    implicit val encoderA: Encoder[A] = Encoder.forProduct2("foo", "bar")(a => (a.foo, a.bar))
    implicit val decoderA: Decoder[A] = Decoder.forProduct2("foo", "bar")(A.apply)
    val aSchema = new DebeziumCompositeType[A] {
      def schema: List[DebeziumFieldSchema] =
        List(
          DebeziumFieldSchema(DebeziumSchemaPrimitive.Int32, false, "foo"),
          DebeziumFieldSchema(DebeziumSchemaPrimitive.String, false, "bar")
        )
    }

    case class B(baz: Boolean, qux: Long)
    implicit val encoderB: Encoder[B] = Encoder.forProduct2("baz", "qux")(a => (a.baz, a.qux))
    implicit val decoderB: Decoder[B] = Decoder.forProduct2("baz", "qux")(B.apply)
    val bSchema = new DebeziumCompositeType[B] {
      def schema: List[DebeziumFieldSchema] =
        List(
          DebeziumFieldSchema(DebeziumSchemaPrimitive.Boolean, false, "baz"),
          DebeziumFieldSchema(DebeziumSchemaPrimitive.Int64, false, "qux")
        )
    }

    val key1 =
      DebeziumKey(DebeziumKeySchema(aSchema.schema, "b.Key"), DebeziumKeyPayload.CompositeKeyPayload(B(true, 32L)))
    val key2 =
      DebeziumKey(DebeziumKeySchema(aSchema.schema, "b.Key"), DebeziumKeyPayload.CompositeKeyPayload(B(false, 64L)))

    val inputA = List(
      DebeziumKey(DebeziumKeySchema(aSchema.schema, "a.Key"), DebeziumKeyPayload.CompositeKeyPayload(A(5, "hello"))) ->
        new DebeziumValue(JsonObject.empty, DebeziumPayload2.InitialPayload(42, Json.obj(), 0L)),
      DebeziumKey(DebeziumKeySchema(aSchema.schema, "a.Key"), DebeziumKeyPayload.CompositeKeyPayload(A(5, "world"))) ->
        new DebeziumValue(JsonObject.empty, DebeziumPayload2.CreatePayload(43, Json.obj(), 0L))
    )
    val inputB = List(
      key1 -> new DebeziumValue(JsonObject.empty, DebeziumPayload2.InitialPayload("hello", Json.obj(), 0L)),
      key2 -> new DebeziumValue(JsonObject.empty, DebeziumPayload2.CreatePayload("world", Json.obj(), 0L))
    )

    val sb = new StreamsBuilder
    val tableA = DebeziumTable.withCompositeKey[A, Int](sb, "a", aSchema)
    val tableB = DebeziumTable.withCompositeKey[B, String](sb, "b", bSchema)

    val result = tableB.joinOption(tableA)(dbz => dbz.payload.after.map(s => A(s.length, s)))((b, a) =>
      s"${a.payload.after}:${b.payload.after}"
    )

    Resource
      .eval(result.to[IO](out) >> IO(sb.build))
      .flatMap(topo => KafkaStreamsTestRunner.testDriverResource[IO](topo))
      .use(driver =>
        KafkaStreamsTestRunner.inputTestTable[IO, CirceCodec](driver, "a", inputA: _*) >>
          KafkaStreamsTestRunner.inputTestTable[IO, CirceCodec](driver, "b", inputB: _*) >>
          KafkaStreamsTestRunner
            .outputTestTable[IO, CirceCodec, DebeziumKey[B], String](driver, out)
            .map(res =>
              assertEquals(
                Map(
                  key1 -> "Some(42):Some(hello)",
                  key2 -> "Some(43):Some(world)"
                ),
                res
              )
            )
      )
      .unsafeToFuture

  }
}
