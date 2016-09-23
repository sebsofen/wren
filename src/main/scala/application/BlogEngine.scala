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
  override implicit val cfg = config
  override val logger = Logging(system, getClass)

  case class BlogSpec(name: String,
                      postdir: String,
                      blogController: PostsController,
                      authorsController: AuthorsController,
                      guiFiles: String)

  lazy val blogspecs = (for {
    entry <- config.getObject("blogs").entrySet
    blogname = entry.getKey
    postsrepo = entry.getValue.atKey(blogname).getString(blogname + ".posts")
    authorsrepo = entry.getValue.atKey(blogname).getString(blogname + ".authors")
    guifiles = entry.getValue.atKey(blogname).getString(blogname + ".guifiles")
    blogController = new PostsController(new PostsRepository(blogname, postsrepo))
    authorsController = new AuthorsController(new AuthorsRepository(blogname, authorsrepo))
  } yield (blogname, BlogSpec(blogname, postsrepo, blogController, authorsController, guifiles))).toMap

  println(blogspecs)

  Http().bindAndHandle(route, config.getString("http.interface"), config.getInt("http.port"))

}
