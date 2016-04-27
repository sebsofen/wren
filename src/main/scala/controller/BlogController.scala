package controller

import data.PostsRepository
import model.Posts
import model.Posts._
import model.PostsHandler.{PostNotFound, BlogError}

import scala.concurrent.Future
/**
  * Created by sebastian on 15/04/16.
  */
class BlogController(implicit pr: PostsRepository) {

  def getPostBySlug(slug: String): Future[Either[BlogError,PostAsm]] = pr.getPostBySlug(slug)
  def getPosts(limit: Int, offset: Int, compact: Boolean, sortBy: (PostMetadata,PostMetadata) => Boolean = Posts.orderByDate, filterBy: PostAsm => Boolean = Posts.filterGetAll) =
    pr.getPosts(limit,offset,compact,sortBy,filterBy)
}
