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
class PostsController(
    pr: PostsRepository)(implicit config: Config, materializer: ActorMaterializer, ec: ExecutionContext) {

  /**
    * simply get post by slug
    * @param slug
    * @return
    */
  def getPostBySlug(slug: String): Future[PostAsm] = pr.getPostBySlug(slug)

  def getFeed(): Future[Feed] = pr.getFeed()

  /**
    * get all posts that satisfy the given parameters
    * @param limit
    * @param offset
    * @param compact
    * @param sortBy
    * @param filterBy
    * @return
    */
  def getPosts(
      limit: Int,
      offset: Int,
      compact: Boolean,
      sortBy: (PostMetadata, PostMetadata) => Boolean,
      filterBy: PostAsm => Boolean,
      reverse: Boolean
  ) = pr.getPosts(limit, offset, compact, sortBy, filterBy, reverse)

  def getPosts(
      limit: Int,
      offset: Int,
      compact: Boolean,
      sortBy: (PostMetadata, PostMetadata) => Boolean = Posts.orderByDate,
      filterBy: List[PostAsm => Boolean] = List(Posts.filterGetAll),
      reverse: Boolean = false
  ) = pr.getPosts(limit, offset, compact, sortBy, filterBy, reverse)

  /**
    * read metadata from repository to generate Blog metainfo
    * @param start
    * @param stop
    * @return
    */
  def getBlogMetaInfo(start: Long, stop: Long) =
    pr.getPostMetadatasUnorderedSource()
      .filter(p => p.created >= start && p.created <= stop)
      .runWith(Sink.fold[BlogMetaInfo, PostMetadata](new BlogMetaInfo())(
              (blog, post) => {
            blog.copy(
                tags = (blog.tags ++ post.tags),
                postCount = blog.postCount + 1,
                start = if (post.created < blog.start) post.created else blog.start,
                stop = if (post.created > blog.stop) post.created else blog.stop
            )
          }
          ))

  def getSimilar(slug: String, limit: Int): Future[Seq[Posts.PostAsm]] = {
    pr.getPostBySlug(slug)
      .map { post =>
        val postVector = vectorizePost(post)
        val allOtherPosts = getPosts(Int.MaxValue, 0, false)
        allOtherPosts.map { f =>
          f.filter(_.metadata != post.metadata)
            .map { f =>
              val vectorized = vectorizePost(f)
              val dist = euclideandist(vectorized, postVector)

              (f, dist)
            }
            .sortBy(_._2)
            .reverse
            .take(limit)
            .map(_._1)
        }
      }
      .flatMap(identity)
  }

  def euclideandist(hist1: Map[String, Int], hist2: Map[String, Int]): Double = {
    var sum = 0.0
    println(hist1)
    println()
    println(hist2)
    val keyset_union = hist1.keySet union hist2.keySet

    for (key <- keyset_union) {
      val v1 = hist1.getOrElse(key, 0)
      val v2 = hist2.getOrElse(key, 0)
      sum += math.pow(v1 - v2, 2)
    }
    println("moooin" + sum)
    math.sqrt(sum)
  }

  def vectorizePost(post: PostAsm): Map[String, Int] =
    post.post.content.split(" ").groupBy(identity).mapValues(_.size)

}
