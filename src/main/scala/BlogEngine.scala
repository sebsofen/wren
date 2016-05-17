import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.{ConfigValue, Config, ConfigFactory}
import java.io.IOException
import data.PostsRepository

import scala.collection.immutable.HashSet
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.math._
import spray.json.DefaultJsonProtocol
import scala.collection.JavaConversions._



/**
  * Created by sebastian on 10/03/16.
  */
object BlogEngine extends App with rest.Router {

  override implicit val system = ActorSystem("blog")
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()
  val config = ConfigFactory.load()
  override implicit val cfg = config
  override val logger = Logging(system, getClass)
  override implicit val postRepository = new PostsRepository()

  case class BlogSpec(name: String, postdir: String)


  val blogspecs = for{
    entry <- config.getObject("blogs").entrySet
    blogname = entry.getKey
    postsrepo = entry.getValue.atKey(blogname).getString(blogname + ".posts")
  } yield BlogSpec(blogname,postsrepo)

  println(blogspecs)



  Http().bindAndHandle(route, config.getString("http.interface"), config.getInt("http.port"))



  //val filepostsrepository = new FilePostsRepository()

  //filepostsrepository.getPostBySlug("bash_rename_filess").foreach( post =>  println(post) )

  //filepostsrepository.getPosts().andThen {
  //  case f => f.get.foreach(println)
 //   }
 // Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
