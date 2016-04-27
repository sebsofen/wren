package model

import akka.actor.Actor.Receive
import akka.actor._
import akka.event.LoggingReceive
import akka.http.scaladsl.model.{StatusCodes, HttpResponse}
import akka.http.scaladsl.server._

import akka.stream.ActorMaterializer
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}


object PostsHandler {
  case class GetPostBySlug(slug: String)
  abstract class BlogError

  case class PostNotFound() extends BlogError

  def props(ctx: RequestContext)(implicit config: Config, materializer :ActorMaterializer, ec: ExecutionContext) : Props = Props(new PostsHandler(ctx))
}


/**
  * Created by sebastian on 27/04/16.
  */
class PostsHandler(ctx: RequestContext) extends Actor with ActorLogging {
  import model.PostsHandler._
  import model.Posts._
  override def receive: Receive = {
    case GetPostBySlug(slug) =>
      ctx.complete(HttpResponse(StatusCodes.NotAcceptable))
    case _ =>
      ctx.complete(HttpResponse(StatusCodes.NotAcceptable))


  }

}
