package application

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import controller.{AuthorsController, PostsController}
import data.{AuthorsRepository, PostsRepository}

import scala.collection.JavaConversions._

/**
  * Created by sebastian on 10/03/16.
  */
object BlogEngine extends App with rest.Router {

  override implicit val system = ActorSystem("blog")
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()
  val config = ConfigFactory.load()
  override implicit val cfg = new ApplicationConfig(config)

  override val logger = Logging(system, getClass)

  case class BlogSpec(name: String,
                      postdir: String,
                      authorsdir: String,
                      blogController: PostsController,
                      authorsController: AuthorsController,
                      guiFiles: String)

  lazy val blogspecs = (for {
    entry <- cfg.BLOGSPECS
  } yield
    (entry.name,
     BlogSpec(entry.name,
              entry.postrepo,
              entry.authorsrepo,
              new PostsController(new PostsRepository(entry.name, entry.postrepo)),
              new AuthorsController(new AuthorsRepository(entry.name, entry.authorsrepo)),
              entry.guiRepo))).toMap

  Http().bindAndHandle(route, config.getString("http.interface"), config.getInt("http.port"))

}
