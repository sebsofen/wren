package data

import java.io.File
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, FlowShape}
import com.typesafe.config.Config
import model.Posts
import model.Posts._
import rest.PostJsonSupport
import spray.json._
import scala.concurrent.{ExecutionContext, Future}



/**
  * Created by sebastian on 11/04/16.
  */
class PostsRepository(implicit config: Config,  materializer :ActorMaterializer, ec: ExecutionContext) extends PostJsonSupport{
  val repodir = config.getString("postsfilerepository.postsdir")

  def getPostBySlug(slug: String) = Source.single(slug).via(slugToMetadata).via(assemblePostFromMetadata).map(f => Right(f)).runWith(Sink.head)



  def getPosts(
                limit: Int,
                offset: Int,
                compact: Boolean,
                sortBy: (PostMetadata,PostMetadata) => Boolean = Posts.orderByDate,
                filterBy: PostAsm => Boolean = Posts.filterGetAll,
                reverse: Boolean = false
              ) =
    getPostMetadatasUnorderedSource()
      .grouped(Int.MaxValue)
      .map(if (reverse) _.sortWith(sortBy) else _.sortWith(sortBy).reverse)
      .map(_.drop(offset).take(limit))
      .map(f => f.map(f =>PostAsm(f,Post(scala.io.Source.fromFile(repodir + "/" + f.slug + "/Post.md").mkString))))
      .runWith(Sink.head)


  /**
    * source for posts metadata
 *
    * @return
    */
  def getPostMetadatasUnorderedSource() = Source.fromIterator(() => new File(repodir).listFiles.filter(_.isDirectory).toIterator)
    .map(f => f.getName)
    .via(slugToMetadata)



  /**
    * from a slug, read metadata
    *
    * @return
    */
  def slugToMetadata()  = Flow[String].map(f => scala.io.Source.fromFile(repodir + "/" + f + "/metadata.json").mkString.parseJson.convertTo[PostMetadata].copy(slug=f))

  def assemblePostFromMetadata() = Flow[PostMetadata].map(f => PostAsm(f,Post(scala.io.Source.fromFile(repodir + "/" + f.slug + "/Post.md").mkString)))


}
