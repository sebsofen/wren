import akka.NotUsed
import akka.stream.ClosedShape
import akka.stream.scaladsl._
import api.Post
import com.typesafe.config.Config
import java.io.File
import scala.concurrent.Future
import spray.json._
import GraphDSL.Implicits._
import DefaultJsonProtocol._
import akka.stream.scaladsl._
/**
  * Created by sebastian on 14/03/16.
  */
class FilePostsRepository(implicit config: Config) extends PostsRepository {
  val postsdir = config.getString("postsfilerepository.postsdir")

  override def getPostBySlug(slug: String): Future[Option[Post]] = {

    //different sources for: metadata retrieval
    //post content
    val metadatasource = FileIO.fromFile(new File(postsdir + "/" + slug + "/metadata.json")).map(contents => {
      val metaObj = contents.toString().parseJson.convertTo[MetadataJson]
      metaObj

    })


    val resultSink = Sink.head[Option[Post]]

    val g = RunnableGraph.fromGraph(GraphDSL.create(resultSink) { implicit b =>
      sink =>
        import GraphDSL.Implicits._

        val postcontentsource = FileIO.fromFile(new File(postsdir + "/" + slug + "/Post.md")).map(contents => {
          contents.toString()
        })
        val mergemetadataanddata = ZipWith[MetadataJson, String, Option[Post]]((metadata, postcontent) => {
          Some(Post(name = metadata.title,content = postcontent, slug = slug))
        })

        metadatasource ~> mergemetadataanddata.in0
        postcontentsource ~> mergemetadataanddata.in1
        mergemetadataanddata.out ~> sink.in
        ClosedShape
    })


    g.run()
  }

  case class MetadataJson(title: String)
}
