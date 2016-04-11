import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import com.typesafe.config.Config
import spray.json.DefaultJsonProtocol
import scala.concurrent.duration._
import akka.pattern.ask

import scala.concurrent.{Future, ExecutionContextExecutor}

/**
  * Created by sebastian on 11/04/16.
  */
trait Protocols extends DefaultJsonProtocol

trait Protocol extends Protocols{
  implicit val system: ActorSystem
  implicit val timeout = Timeout(5 seconds)
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer
  implicit val cfg: Config
  val logger: LoggingAdapter


  lazy val blogActor = system.actorOf(BlogActor.props(), "blog")

  def routes: Route =  logRequestResult("akka-http-test") {
    import BlogActor._

    path("posts" / "by-slug" / Segment) { slug =>

      val req = blogActor ? GetPostBySlug(slug)

      //TDO IDK complete{req}





    }

  }




  }
