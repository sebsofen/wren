package model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * Created by sebastian on 11/04/16.
  */

object Posts {
  case class Post(content: String)
  case class PostMetadata(title:String, created: Long, tags: Set[String] = Set(), slug: String, author: Option[String])
  case class PostAsm(metadata: PostMetadata,post: Post)

  case class BlogMetaInfo(
                           tags: Set[String] = Set(),
                           start: Long = Long.MaxValue,
                           stop: Long = Long.MinValue,
                           postCount : Int = 0)

  def orderByDate(m1 : PostMetadata, m2: PostMetadata) = m1.created < m2.created

  /**
    * generic function to allow all posts (default)
    * @param m
    * @return
    */
  def filterGetAll(m : PostAsm) = true
   /**
    * filter posts by tags, allow all posts that match at least one tag in tags
    * @param tags tags to be matched
    */
  def filterByTags(tags: Set[String]) : PostAsm => Boolean = m => m.metadata.tags.intersect(tags).nonEmpty
}

object Authors {
  case class Author(name: String, imgurl: Option[String])
}



