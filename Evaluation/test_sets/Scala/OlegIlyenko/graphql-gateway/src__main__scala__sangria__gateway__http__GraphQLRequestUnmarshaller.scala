package sangria.gateway.http

import java.nio.charset.Charset

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import sangria.ast.Document
import sangria.parser.QueryParser
import sangria.renderer.{QueryRenderer, QueryRendererConfig}

import scala.collection.immutable.Seq

object GraphQLRequestUnmarshaller {
  val `application/graphql` = MediaType.applicationWithFixedCharset("graphql", HttpCharsets.`UTF-8`, "graphql")

  def explicitlyAccepts(mediaType: MediaType): Directive0 =
    headerValuePF {
      case Accept(ranges) if ranges.exists(range ⇒ !range.isWildcard && range.matches(mediaType)) ⇒ ranges
    }.flatMap(_ ⇒ pass)

  def includeIf(include: Boolean): Directive0 =
    if (include) pass
    else reject

  def unmarshallerContentTypes: Seq[ContentTypeRange] =
    mediaTypes.map(ContentTypeRange.apply)

  def mediaTypes: Seq[MediaType.WithFixedCharset] =
    List(`application/graphql`)

  implicit final def documentMarshaller(implicit config: QueryRendererConfig = QueryRenderer.Compact): ToEntityMarshaller[Document] =
    Marshaller.oneOf(mediaTypes: _*) { mediaType ⇒
      Marshaller.withFixedContentType(ContentType(mediaType)) { json ⇒
        HttpEntity(mediaType, QueryRenderer.render(json, config))
      }
    }

  /**
    * HTTP entity => `Json`
    *
    * @return unmarshaller for `Json`
    */
  implicit final val documentUnmarshaller: FromEntityUnmarshaller[Document] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .map {
        case ByteString.empty ⇒ throw Unmarshaller.NoContentException
        case data ⇒
          import sangria.parser.DeliveryScheme.Throw

          QueryParser.parse(data.decodeString(Charset.forName("UTF-8")))
      }
}
