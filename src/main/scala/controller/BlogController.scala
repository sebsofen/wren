package controller

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.Config
import data.PostsRepository
import model.Posts
import model.Posts._
import model.PostsHandler.{PostNotFound, BlogError}

import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by sebastian on 15/04/16.
  */
class BlogController(implicit pr: PostsRepository, config: Config,  materializer :ActorMaterializer, ec: ExecutionContext) {

  def getPostBySlug(slug: String): Future[Either[BlogError,PostAsm]] = pr.getPostBySlug(slug)
  def getPosts(limit: Int, offset: Int, compact: Boolean, sortBy: (PostMetadata,PostMetadata) => Boolean = Posts.orderByDate, filterBy: PostAsm => Boolean = Posts.filterGetAll) =
    pr.getPosts(limit,offset,compact,sortBy,filterBy)

  def getBlogMetaInfo(start: Long, stop: Long) = {
    pr.getPostMetadatasUnorderedSource()
      .filter(p => p.created >= start && p.created <= stop)
      .runWith(Sink.fold[BlogMetaInfo,PostMetadata](new BlogMetaInfo())(
        (blog, post) => {
          blog.copy(
            tags = (blog.tags ++ post.tags),
            postCount = blog.postCount + 1,
            start = if (post.created < blog.start) post.created else blog.start,
            stop = if (post.created > blog.stop) post.created else blog.stop
          )
        }
      ))
  }
}
