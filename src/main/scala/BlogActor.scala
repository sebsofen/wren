import akka.actor.{Props, Actor}
import akka.actor.Actor.Receive
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import akka.pattern.ask

import scala.concurrent.ExecutionContext

/**
  * Created by sebastian on 11/04/16.
  */

object BlogActor {
  def props()(implicit config: Config,  materializer :ActorMaterializer, ec: ExecutionContext) : Props = Props(new BlogActor())

  case class GetPostBySlug(slug: String)
  case class GetNothing()

}

class BlogActor(implicit config: Config,  materializer :ActorMaterializer, ec: ExecutionContext) extends Actor {
  import BlogActor._

  val posts = new PostsRepository()
  override def receive: Receive = {

    case GetPostBySlug(x) =>
      sender() ! posts.getPostBySlug(x)


  }
}
