import scala.concurrent.Future
import model.Posts._
/**
  * Created by sebastian on 15/04/16.
  */
class BlogController(implicit pr: PostsRepository) {

  def getPostBySlug(slug: String): Future[PostAsm] = {
    val post = pr.getPostBySlug(slug)

    post
  }
}
