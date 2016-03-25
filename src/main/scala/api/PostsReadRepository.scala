package api

/**
  * Created by sebastian on 10/03/16.
  */
trait PostsReadRepository {

  def getPostBySlug(slug: String) : Post
  def getPosts(offset: Int, limit: Int, order: PostCompare) : Seq[Post]

}
