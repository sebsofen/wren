package application

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import controller.{AuthorsController, PostsController}
import data.{AuthorsRepository, PostsRepository}
import rest.Router

import scala.collection.JavaConversions._

/**
  * Created by sebastian on 10/03/16.
  */
object BlogEngine extends App  {

  implicit val system = ActorSystem("blog")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val config = ConfigFactory.load()
  implicit val cfg = new ApplicationConfig(config)

  val logger = Logging(system, getClass)

  case class BlogSpec(name: String,
                      postdir: String,
                      authorsdir: String,
                      blogController: PostsController,
                      authorsController: AuthorsController,
                      guiFiles: String)


  //create new route for each port

  cfg.BLOGSPECS.groupBy(_.interfacePort).foreach {
    case (ip,blogSpecSeq) =>
      val blogspecs = (for {
        entry <- cfg.BLOGSPECS
      } yield
        (entry.name,
          BlogSpec(entry.name,
            entry.postrepo,
            entry.authorsrepo,
            new PostsController(new PostsRepository(entry.name, entry.postrepo)),
            new AuthorsController(new AuthorsRepository(entry.name, entry.authorsrepo)),
            entry.guiRepo))).toMap
      val router = new Router(blogspecs,ip)
      Http().bindAndHandle(router.route, ip.interface, ip.port)

  }

}
