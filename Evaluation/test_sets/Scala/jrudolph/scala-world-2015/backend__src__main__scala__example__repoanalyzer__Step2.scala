package example.repoanalyzer

import scala.concurrent.Future
import akka.util.ByteString
import akka.stream.scaladsl.{ Flow, Source }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

object Step2 extends Scaffolding with App {

  def logLinesStreamFuture: Future[Source[String, Any]] =
    Http().singleRequest(logStreamRequest).map { // Future[HttpResponse]
      _.entity.dataBytes
        // .via(Gzip.decoderFlow)
        .map(_.utf8String)
        .via(logFlow)
    }

  runWebService {
    get {
      onSuccess(logLinesStreamFuture) { stream ⇒
        complete {
          HttpResponse(
            entity = HttpEntity.Chunked(
              MediaTypes.`text/plain`,
              stream.map(line ⇒ ByteString(line + '\n', "UTF8"))))
        }
      }
    }
  }

  def logFlow = Flow[String].map { line ⇒
    println(line.slice(6, 80) + "...")
    line
  }
}
