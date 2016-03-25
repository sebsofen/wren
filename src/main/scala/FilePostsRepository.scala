import akka.stream.{ActorMaterializer, ClosedShape}
import akka.util.ByteString
import api.Post
import com.typesafe.config.Config
import java.io.File
import scala.concurrent.Future
import spray.json._
import DefaultJsonProtocol._
import akka.stream.scaladsl._

/**
  * Created by sebastian on 14/03/16.
  */
class FilePostsRepository(implicit config: Config,  materializer :ActorMaterializer) extends PostsRepository {
  implicit val personFormat: JsonReader[MetadataJson] = jsonFormat1(MetadataJson)
  val postsdir = config.getString("postsfilerepository.postsdir")

  override def getPostBySlug(slug: String): Future[Option[Post]] = {
    val postContentFile = new File(postsdir + "/" + slug + "/Post.md")
    val postMetadataFile = new File(postsdir + "/" + slug + "/metadata.json")



    val g = RunnableGraph.fromGraph(GraphDSL.create(Sink.head[Option[Post]]) { implicit builder =>
      sink =>
        import GraphDSL.Implicits._


        val concatenator = Flow[ByteString].map(_.utf8String).grouped(Int.MaxValue).map(_.mkString)

        val postcontentsource = builder.add(FileIO.fromFile(postContentFile).via(concatenator))

        val metadatasource = builder.add(FileIO.fromFile(postMetadataFile).via(concatenator).map(_.parseJson.convertTo[MetadataJson]))
        
        val mergemetadataanddata = builder.add(ZipWith[MetadataJson, String, Option[Post]]((metadata, postcontent) => {
          Some(Post(name = metadata.title,content = postcontent, slug = slug))
        }))



        metadatasource ~> mergemetadataanddata.in0
        postcontentsource ~> mergemetadataanddata.in1
        mergemetadataanddata.out ~> sink.in
        ClosedShape
    })


    g.run()
  }

  case class MetadataJson(title: String)
}
