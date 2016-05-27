package rest

//critical for path directives etc
import akka.actor.ActorSystem
import akka.actor._
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.actor._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server._
import application.BlogEngine
import data.PostsRepository
import scala.concurrent.{Promise, Future, ExecutionContextExecutor}
import akka.http.scaladsl.model.{StatusCodes, HttpResponse}
import akka.util.Timeout
import com.typesafe.config.{ConfigFactory, Config}
import model.Posts._
import model.{Posts, PostsHandler}
import model.PostsHandler.{PostNotFound, BlogError, GetPostBySlug}
import spray.json.DefaultJsonProtocol
import akka.actor.{ActorSystem, Actor, Props}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import akka.util.Timeout
import scala.concurrent.duration._
import scala.util.Success

import controller.PostsController



/**
  * Created by sebastian on 27/04/16.
  */
trait Router extends PostJsonSupport with CorsSupport{

  val system = ActorSystem("Actor")
  implicit val timeout = Timeout(5 seconds)

  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer
  implicit val cfg: Config
  val logger: LoggingAdapter



  val route = corsHandler {
    pathPrefix("v1" / Segment) { blog : String =>

      BlogEngine.blogspecs.get(blog) match {
        case Some(x) => blogroute(x)
        case None => complete {HttpResponse(StatusCodes.NotFound)}
      }

    }
  }

  def blogroute(blog:BlogEngine.BlogSpec) = path("posts" / "by-slug" / Segment) { slug: String =>
        get {

          complete {
            blog.blogController.getPostBySlug(slug).map[ToResponseMarshallable] {
              case post => post
            }
          }
        }
      } ~
      path("posts" / "by-tags" / Segment) { tags: String =>
        println( blog + tags)
        parameters('limit.as[Int] ? 10, 'offset.as[Int] ? 0, 'order.as[String] ? "bydate", 'compact.as[Boolean] ? false, 'sort.as[String] ? "desc") { (limit, offset, order, compact, sort) =>
          get {
            complete {
              blog.blogController.getPosts(limit, offset, compact, orderStrToFunc(order),filterBy = Posts.filterByTags(tags.split(",").toSet),reverse = sort.equals("desc")).map[ToResponseMarshallable] {
                case f => f
              }
            }
          }
        }
      } ~
      path("posts" / "by-search" / Segment) { search: String =>
        parameters('limit.as[Int] ? 10, 'offset.as[Int] ? 0, 'order.as[String] ? "bydate", 'compact.as[Boolean] ? false, 'sort.as[String] ? "desc") { (limit, offset, order, compact, sort) =>
          get {
            complete {
              blog.blogController.getPosts(limit, offset, compact, orderStrToFunc(order),filterBy = Posts.filterBySearchStr(search),reverse = sort.equals("desc")).map[ToResponseMarshallable] {
                case f => f
              }
            }
          }
        }
      } ~
      path("posts") {
        parameters('limit.as[Int] ? 10, 'offset.as[Int] ? 0, 'order.as[String] ? "bydate", 'compact.as[Boolean] ? false, 'sort.as[String] ? "desc") { (limit, offset, order, compact, sort) =>
          complete {
            blog.blogController.getPosts(limit, offset, compact, orderStrToFunc(order), reverse = sort.equals("desc")).map[ToResponseMarshallable] {
              case f => f
            }
          }
        }
      } ~
      path("blog" / "metainfo" ) {
        parameters('start.as[Long] ? 0, 'stop.as[Long] ? Long.MaxValue) { (start,stop) =>
          get {
            complete {
              blog.blogController.getBlogMetaInfo(start,stop).map[ToResponseMarshallable] {
                case f => f
              }
            }
          }
        }

      } ~
      pathPrefix("static") {
        encodeResponse {
          getFromDirectory(cfg.getString("postsfilerepository.postsdir"))
        }
      }



  def orderStrToFunc(order: String): (PostMetadata,PostMetadata) => Boolean = order match {
    case "bydate" => Posts.orderByDate
    case _ => Posts.orderByDate
  }
}



