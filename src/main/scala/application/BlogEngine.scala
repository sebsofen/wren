package application

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import controller.PostsController
import data.PostsRepository

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


  case class BlogSpec(name: String, postdir: String, blogController: PostsController)


  lazy val blogspecs = (for{
    entry <- config.getObject("blogs").entrySet
    blogname = entry.getKey
    postsrepo = entry.getValue.atKey(blogname).getString(blogname + ".posts")
    blogController = new PostsController(new PostsRepository(postsrepo))
  } yield (blogname,BlogSpec(blogname,postsrepo,blogController))).toMap



  println(blogspecs)



  Http().bindAndHandle(route, config.getString("http.interface"), config.getInt("http.port"))



  //val filepostsrepository = new FilePostsRepository()

  //filepostsrepository.getPostBySlug("bash_rename_filess").foreach( post =>  println(post) )

  //filepostsrepository.getPosts().andThen {
  //  case f => f.get.foreach(println)
 //   }
 // Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))


}
