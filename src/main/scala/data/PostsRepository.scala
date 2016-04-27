package data

import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, FlowShape}
import com.typesafe.config.Config
import model.Posts._
import spray.json._
import scala.concurrent.{ExecutionContext, Future}



/**
  * Created by sebastian on 11/04/16.
  */
class PostsRepository(implicit config: Config,  materializer :ActorMaterializer, ec: ExecutionContext) extends PostJsonSupport{
  val repodir = config.getString("postsfilerepository.postsdir")

  def getPostBySlug(slug: String) = Source.single(slug).via(slugToMetadata).via(assemblePostFromMetadata).map(f => Right(f)).runWith(Sink.head)





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

  /**
    * from a slug, read metadata
    * @return
    */
  def slugToMetadata()  = Flow[String].map(f => scala.io.Source.fromFile(repodir + "/" + f + "/metadata.json").mkString.parseJson.convertTo[PostMetadata].copy(slug=f))

  def assemblePostFromMetadata() = Flow[PostMetadata].map(f => PostAsm(f,Post(scala.io.Source.fromFile(repodir + "/" + f.slug + "/Post.md").mkString)))


}
