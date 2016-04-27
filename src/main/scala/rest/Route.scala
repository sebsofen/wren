package rest

//critical for path directives etc
import akka.actor.ActorSystem
import akka.actor._
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.actor.{ActorSystem, Actor, Props}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import scala.concurrent.Future
import akka.http.scaladsl.model.HttpResponse
import akka.util.Timeout
import com.typesafe.config.Config
import model.Posts._
import model.PostsHandler
import model.PostsHandler.GetPostBySlug
import spray.json.DefaultJsonProtocol
import akka.actor.{ActorSystem, Actor, Props}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import scala.concurrent.Future
import akka.http.scaladsl.model.HttpResponse
import akka.util.Timeout
import scala.concurrent.ExecutionContextExecutor
import akka.util.Timeout
import scala.concurrent.duration._

trait PostJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val postMetadataFormat = jsonFormat4(PostMetadata)
  implicit val postFormat = jsonFormat1(Post)
  implicit val postAsmFormat = jsonFormat2(PostAsm)
}

/**
  * Created by sebastian on 27/04/16.
  */
trait Route extends PostJsonSupport{
   val system = ActorSystem("Actor")
  implicit val timeout = Timeout(5 seconds)
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer
  implicit val cfg: Config
  val logger: LoggingAdapter

  lazy val postsHandler = system.actorOf(PostsHandler.props(), "blog")
  val route = {
    path("posts" / "by-slug" / Segment ) { slug =>
      get {
        complete{
          Future.successful(Some(PostAsm(PostMetadata("hi", 123, slug = slug),Post("hi")))).map[ToResponseMarshallable] {
            case Some(bla) => bla
          }

        }

      }

    }
  }
}
