package model

import akka.http.scaladsl.model.DateTime

/**
  * Created by sebastian on 11/04/16.
  */
object Posts {

  case class Post(content: String)
  case class PostMetadata(title: String,
                          created: Long,
                          tags: Set[String] = Set(),
                          slug: Option[String],
                          authors: Option[Set[String]],
                          coverImage: Option[String]) {
    def getSlug = slug.get
  }
  case class PostAsm(metadata: PostMetadata, post: Post)

  case class BlogMetaInfo(
      tags: Set[String] = Set(),
      start: Long = Long.MaxValue,
      stop: Long = Long.MinValue,
      postCount: Int = 0
  )

  case class Feed(meta: FeedMeta, posts: Seq[PostAsm])

  case class FeedMeta(blogname: String,
                      updated: DateTime,
                      blogurl: String,
                      id: String,
                      author: String,
                      postsUrlPref: String)

  def orderByDate: (PostMetadata, PostMetadata) => Boolean =
    (m1, m2) => m1.created < m2.created

  /**
    * generic function to allow all posts (default)
    * @param m
    * @return
    */
  def filterGetAll(m: PostAsm) = true

  def filterGetAllFunc: PostAsm => Boolean = m => true

  def filterBySlug(slug: String): PostAsm => Boolean =
    m => m.metadata.slug.get == slug

  /**
    * filter posts by tags, allow all posts that match at least one tag in tags
    * @param tags tags to be matched
    */
  def filterByTags(tags: Set[String]): PostAsm => Boolean =
    m => m.metadata.tags.intersect(tags).nonEmpty

  def filterByDate(start: Long, stop: Long): PostAsm => Boolean =
    m => m.metadata.created >= start && m.metadata.created <= stop

  /**
    * basci filter function, should be fuzzy in future
    * @param searchStr
    * @return
    */
  def filterBySearchStr(searchStr: String): PostAsm => Boolean =
    m =>
      (m.post.content.toLowerCase contains searchStr.toLowerCase) || (m.metadata.title.toLowerCase contains searchStr.toLowerCase)

  def emptyPost =
    PostAsm(PostMetadata("d", 1, Set.empty[String], Some("hi"), None, None), Post("hi"))
}
