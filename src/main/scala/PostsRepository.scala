import akka.stream.{ActorMaterializer, FlowShape}
import akka.stream.javadsl.Unzip
import akka.stream.scaladsl._
import akka.util.ByteString
import com.typesafe.config.Config
import model.Posts._
import scala.concurrent.{ExecutionContext, Future}



/**
  * Created by sebastian on 11/04/16.
  */
class PostsRepository(implicit config: Config,  materializer :ActorMaterializer, ec: ExecutionContext) {
  def getPostBySlug(slug: String) = Source.single(slug).via(bytestreamflow).mapAsync(1){f => Future.successful(new PostAsm(f._1,f._2))}.runWith(Sink.head)





  val  bytestreamflow: Flow[String,Tuple2[PostMetadata,Post], Unit] = Flow.fromGraph(GraphDSL.create() {
    implicit b =>
      import GraphDSL.Implicits._
      val stringToMetadata = b.add(Flow[String].map(f => new PostMetadata(title = "hi", created = 2l, slug = "uh" )))
      val stringToPost = b.add(Flow[String].map(f => new Post("bro")))

      val slugBroadcast = b.add(Broadcast[String](2))
      val zipMetadataPost = b.add(Zip[PostMetadata,Post])

      slugBroadcast ~> stringToMetadata ~> zipMetadataPost.in0
      slugBroadcast ~> stringToPost~> zipMetadataPost.in1

      FlowShape(slugBroadcast.in,zipMetadataPost.out)
  })



}
