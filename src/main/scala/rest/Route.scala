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
import application.{ApplicationConfig, BlogEngine, InterfacePort}
import data.PostsRepository

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future, Promise}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import model.Posts._
import model.{Posts, PostsHandler}
import model.PostsHandler.{BlogError, GetPostBySlug, PostNotFound}
import spray.json.DefaultJsonProtocol
import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import application.BlogEngine.BlogSpec

import scala.concurrent.duration._

class Router(blogSpec: Map[String, BlogEngine.BlogSpec],
             interface: InterfacePort)(implicit system: ActorSystem,
                                       _ec: ExecutionContextExecutor,
                                       materializer: ActorMaterializer,
                                       conf: ApplicationConfig)
    extends RouterTrait {

  val config = ConfigFactory.load()
  implicit val cfg = new ApplicationConfig(config)
  override val blogspecs: Map[String, BlogSpec] = blogSpec
  override val defaultblog: (String, BlogSpec) = blogSpec.head
  override implicit val ec: ExecutionContext = _ec
}

/**
  * Created by sebastian on 27/04/16.
  */
trait RouterTrait
    extends PostMarshalSupport
    with AuthorMarshalSupport
    with CorsSupport {

  implicit val ec: ExecutionContext
  implicit val timeout = Timeout(5 seconds)
  val blogspecs : Map[String, BlogEngine.BlogSpec]
  val defaultblog   : Tuple2[String, BlogEngine.BlogSpec]
  //implicit val executor: ExecutionContextExecutor
  //implicit val materializer: ActorMaterializer
  //implicit val cfg: ApplicationConfig
  //val logger: LoggingAdapter

  val route = corsHandler {
    pathPrefix("v1" / Segment) { blog: String =>
      blogspecs.get(blog) match {
        case Some(x) => blogroute(x)
        case None =>
          complete {
            HttpResponse(StatusCodes.NotFound)
          }
      }
    } ~
      pathPrefix("") {
        encodeResponse {
          getFromDirectory(defaultblog._2.guiFiles) // return first blog as default

        }
      } ~
      pathPrefix("") {
        getFromFile(defaultblog._2.guiFiles + "/index.html")
      }
  }

  def blogroute(blog: BlogEngine.BlogSpec) =
    path("authors" / "by-name" / Segment) { name: String => //implement split by , for multiple authors requests!
      get {
        complete {
          blog.authorsController
            .getAuthorByName(name.split(","))
            .map[ToResponseMarshallable] { case author => author }
        }
      }

    } ~
      path("authors") {
        get {
          complete {
            blog.authorsController
              .getAuthorsList()
              .map[ToResponseMarshallable] { case authors => authors }
          }
        }
      } ~
      path("posts" / "similar" / Segment) { slug: String =>
        get {
          complete {
            blog.blogController
              .getSimilar(slug, 3)
              .map[ToResponseMarshallable] { case post => post }
          }
        }
      } ~
      path("posts" / "by-slug" / Segment) { slug: String =>
        get {
          complete {
            blog.blogController
              .getPostBySlug(slug)
              .map[ToResponseMarshallable] { case post => post }
          }
        }
      } ~
      path("posts" / "by-tags" / Segment) { tags: String =>
        println(blog + tags)
        parameters('limit.as[Int] ? 10,
                   'offset.as[Int] ? 0,
                   'order.as[String] ? "bydate",
                   'compact.as[Boolean] ? false,
                   'sort.as[String] ? "desc") {
          (limit, offset, order, compact, sort) =>
            get {
              complete {
                blog.blogController
                  .getPosts(limit,
                            offset,
                            compact,
                            orderStrToFunc(order),
                            filterBy =
                              Posts.filterByTags(tags.split(",").toSet),
                            reverse = sort.equals("desc"))
                  .map[ToResponseMarshallable] {
                    case f => f
                  }
              }
            }
        }
      } ~
      path("posts" / "filter" / Segments) { filters: List[String] =>
        parameters('limit.as[Int] ? 10,
                   'offset.as[Int] ? 0,
                   'order.as[String] ? "bydate",
                   'compact.as[Boolean] ? false,
                   'sort.as[String] ? "desc") {
          (limit, offset, order, compact, sort) =>
            get {
              complete {
                val filterfuncs = for {
                  filter <- filters
                  filtersplitted = filter.split(":")
                  fltrfunc = filtersplitted.head match {
                    case "tags" =>
                      Posts.filterByTags(filtersplitted.last.split(",").toSet)
                    case "date" =>
                      val splitted =
                        filtersplitted.last.split(",").map(_.toLong)
                      Posts.filterByDate(splitted(0), splitted(1))
                    case _ => Posts.filterGetAllFunc
                  }
                } yield fltrfunc

                blog.blogController
                  .getPosts(limit,
                            offset,
                            compact,
                            orderStrToFunc(order),
                            filterBy = filterfuncs,
                            reverse = sort.equals("desc"))
                  .map[ToResponseMarshallable] {
                    case f => f
                  }
              }
            }
        }
      } ~
      path("posts" / "by-search" / Segment) { search: String =>
        parameters('limit.as[Int] ? 10,
                   'offset.as[Int] ? 0,
                   'order.as[String] ? "bydate",
                   'compact.as[Boolean] ? false,
                   'sort.as[String] ? "desc") {
          (limit, offset, order, compact, sort) =>
            get {
              complete {
                blog.blogController
                  .getPosts(limit,
                            offset,
                            compact,
                            orderStrToFunc(order),
                            filterBy = Posts.filterBySearchStr(search),
                            reverse = sort.equals("desc"))
                  .map[ToResponseMarshallable] {
                    case f => f
                  }
              }
            }
        }
      } ~
      path("posts") {
        parameters('limit.as[Int] ? 10,
                   'offset.as[Int] ? 0,
                   'order.as[String] ? "bydate",
                   'compact.as[Boolean] ? false,
                   'sort.as[String] ? "desc") {
          (limit, offset, order, compact, sort) =>
            complete {
              blog.blogController
                .getPosts(limit,
                          offset,
                          compact,
                          filterBy = Posts.filterGetAllFunc,
                          sortBy = orderStrToFunc(order),
                          reverse = sort.equals("desc"))
                .map[ToResponseMarshallable] {
                  case f => f
                }
            }
        }
      } ~
      path("blog" / "metainfo") {
        parameters('start.as[Long] ? 0, 'stop.as[Long] ? Long.MaxValue) {
          (start, stop) =>
            get {
              complete {
                blog.blogController
                  .getBlogMetaInfo(start, stop)
                  .map[ToResponseMarshallable] {
                    case f => f
                  }
              }
            }
        }

      } ~
      pathPrefix("static" / "posts") {
        encodeResponse {
          getFromDirectory(blog.postdir)
        }
      } ~
      pathPrefix("static" / "authors") {
        encodeResponse {
          getFromDirectory(blog.authorsdir)
        }
      } ~
      path("feed") {
        complete(blog.blogController.getFeed())
      } ~
      pathPrefix("") {
        encodeResponse {
          getFromDirectory(blog.guiFiles)
          //getFromFile()
        }
      }

  def orderStrToFunc(order: String): (PostMetadata, PostMetadata) => Boolean =
    order match {
      case "bydate" => Posts.orderByDate
      case _ => Posts.orderByDate
    }
}
