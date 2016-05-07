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


}

