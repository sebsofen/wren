package controller

import data.PostsRepository
import model.Posts._
import model.PostsHandler.{PostNotFound, BlogError}

import scala.concurrent.Future
/**
  * Created by sebastian on 15/04/16.
  */
class BlogController(implicit pr: PostsRepository) {

  def getPostBySlug(slug: String): Future[Either[BlogError,PostAsm]] = pr.getPostBySlug(slug)

}
