import api.Post
import scala.concurrent.{ExecutionContextExecutor, Future}
/**
  * Created by sebastian on 14/03/16.
  */
trait PostsRepository {
  def getPostBySlug(slug: String) : Future[Option[Post]]

}
