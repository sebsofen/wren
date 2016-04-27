package rest

//critical for path directives etc
import akka.actor.ActorSystem
import akka.actor._
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.actor._
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import data.{PostJsonSupport, PostsRepository}
import scala.concurrent.{Promise, Future, ExecutionContextExecutor}
import akka.http.scaladsl.model.{StatusCodes, HttpResponse}
import akka.util.Timeout
import com.typesafe.config.Config
import model.Posts._
import model.{Posts, PostsHandler}
import model.PostsHandler.{PostNotFound, BlogError, GetPostBySlug}
import spray.json.DefaultJsonProtocol
import akka.actor.{ActorSystem, Actor, Props}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import akka.util.Timeout
import akka.util.Timeout
import scala.concurrent.duration._
import scala.util.Success

import controller.BlogController



/**
  * Created by sebastian on 27/04/16.
  */
trait Route extends PostJsonSupport{
   val system = ActorSystem("Actor")
  implicit val timeout = Timeout(5 seconds)

  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer
  implicit val cfg: Config
  implicit val postRepository : PostsRepository

  val logger: LoggingAdapter

  lazy val blogController = new BlogController
  val route = {
    path("posts" / "by-slug" / Segment) { slug =>
      get {

        complete {
          blogController.getPostBySlug(slug).map[ToResponseMarshallable] {
            case Right(post) => post
            case Left(err) =>
              err match {
                case PostNotFound() => HttpResponse(StatusCodes.NotFound)
              }

          }
        }
      }
    } ~
      path("posts") {
        parameters('limit.as[Int] ? 10, 'offset.as[Int] ? 0, 'order.as[String] ? "bydate", 'compact.as[Boolean] ? true, 'sort.as[String] ? "desc" ) { (limit, offset, order, compact,sort) =>
          complete {
            blogController.getPosts(limit,offset,compact,orderStrToFunc(order)).map[ToResponseMarshallable] {
              case f => if (sort.equals("desc"))   f.reverse else f
            }
          }
        }
      }
  }

  def orderStrToFunc(order: String): (PostMetadata,PostMetadata) => Boolean = order match {
    case "bydate" => Posts.orderByDate
    case _ => Posts.orderByDate
  }
}
