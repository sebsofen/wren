package model

import akka.actor.Actor.Receive
import akka.actor._
import akka.event.LoggingReceive
import akka.stream.ActorMaterializer
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}


object PostsHandler {
  case class GetPostBySlug(slug: String)
  def props()(implicit config: Config,  materializer :ActorMaterializer, ec: ExecutionContext) : Props = Props(new PostsHandler())
}


/**
  * Created by sebastian on 27/04/16.
  */
class PostsHandler extends Actor with ActorLogging {
  import model.PostsHandler._
  import model.Posts._
  override def receive: Receive = {
    case GetPostBySlug(slug) =>
      sender ! Future.successful(PostAsm(PostMetadata("hi", 123, slug = slug),Post("hi")))
  }

}
