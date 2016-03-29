import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.config.Config
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContextExecutor

trait Protocols extends DefaultJsonProtocol

/**
  * Created by sebastian on 14/03/16.
  */
trait Protocol extends Protocols {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer


  def config: Config

  val logger: LoggingAdapter

  //routes for server!
  def routes: Route = logRequestResult("akka-http-test") {

    path("posts" ) {
      get {
        parameters('start.as[Int].?, 'stop) {  (maybeStart , stop) =>
          complete {
            "hii" + "as"
          }
        } //~
      }

    } ~
    path("posts" / "by-slug" / Segment) { slug =>
      complete {
        "hi" + slug
      }

    }
  }
}
