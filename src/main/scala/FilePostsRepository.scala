import akka.stream.{FlowShape, ActorMaterializer, ClosedShape}
import akka.util.ByteString
import api.{PostMetadatas, PostMetadata, Post}
import com.typesafe.config.Config
import java.io.File
import scala.concurrent.{ExecutionContext, Future}
import spray.json._
import DefaultJsonProtocol._
import akka.stream.scaladsl._

/**
  * Created by sebastian on 14/03/16.
  */
class FilePostsRepository(implicit config: Config,  materializer :ActorMaterializer, ec: ExecutionContext) {
  implicit val personFormat: JsonReader[PostMetadata] = jsonFormat4(PostMetadata)
  val postsdir = config.getString("postsfilerepository.postsdir")

  val concatenator = Flow[ByteString].map(_.utf8String).grouped(Int.MaxValue).map(_.mkString)


  def getPosts(offset: Int = 0, limit: Int = 10, sortBy: (PostMetadata, PostMetadata) => Boolean = PostMetadatas.sortByCreationDate, filterBy: (PostMetadata) => Boolean = PostMetadatas.filterGetAll) : Future[Seq[Post]] = {
    val postsiterator = (new File(postsdir + "")).listFiles.toIterator
    val postssource = Source.fromIterator(() => postsiterator).filter(_.isDirectory)

    //val metamaterializer  = new FileToMetadataStage

    //postssource.via(metamaterializer).runForeach(f => println(f))

    val byteStringToMetadata = Flow[File].map(f => FileIO.fromFile(new File(f.getPath + "/metadata.json"))
        .map(_.utf8String)
        .grouped(Int.MaxValue)
        .map(_.mkString)
        .map(f => f.parseJson.convertTo[PostMetadata])
        .runWith(Sink.head[PostMetadata])
    )


    val metadataFlowPost = Flow[PostMetadata].map(f => {
      Post(getPostContentBySlug(f.slug),f)
    })

    val floow = Flow[PostMetadata].map(identity)

    postssource.via(byteStringToMetadata)
      .mapAsync(1) (identity)
      .filter(filterBy)
      .grouped(Int.MaxValue).map(f => f.sortWith(sortBy).slice(offset,offset + limit))
      .map( metaseq => metaseq.map(f => Post(getPostContentBySlug(f.slug),f)))
        .runWith(Sink.head[Seq[Post]])



    //.filter(p => p.value.get.get.created > 0)


    /*
        val  bytestreamflow: Flow[Source[ByteString, akka.NotUsed],PostMetadata, Unit] = Flow.fromGraph(GraphDSL.create() {
          implicit b =>
            import GraphDSL.Implicits._
            val byteStringFlow = b.add(Flow[ByteString].map(_.utf8String).grouped(Int.MaxValue).map(_.mkString))

            val metadataFlow = b.add(Flow[String].map(f => f.parseJson.convertTo[PostMetadata]))

            byteStringFlow ~> metadataFlow

            FlowShape(byteStringFlow.in, metadataFlow.out)
        })


        val  bytestreamflow: Flow[ByteString,PostMetadata, Unit] = Flow.fromGraph(GraphDSL.create() {
          implicit b =>
            import GraphDSL.Implicits._
            val filetostring = b.add(Flow[ByteString].map(f=> {
              println("entering flow")
              f.utf8String
            }).grouped(Int.MaxValue).map(f => {
              println("mapping")
              f.mkString

            }) )
            val stringtometadata = b.add(Flow[String].map(f => f.parseJson.convertTo[PostMetadata]))


            filetostring ~> stringtometadata
            FlowShape(filetostring.in,stringtometadata.out)
        })

        postssource.map(f =>{ FileIO.fromFile({
          val file =  new File(f.getPath + "/metadata.json")
          println("fileexistis? " + file.exists())
          file
        }).via(bytestreamflow).to(Sink.foreach(f => println(f)))}).runForeach(f => f.run())

    */
  }

  def getPostContentBySlug(slug: String) : Future[String] = {
    val filesource = FileIO.fromFile(new File(postsdir + "/" + slug +  "/Post.md"))

    val fileToStringFlow = Flow[ByteString]
      .map(_.utf8String)
      .grouped(Int.MaxValue)
      .map(_.mkString)

    filesource.via(fileToStringFlow).runWith(Sink.head[String])
  }



  /*
  override def getPostBySlug(slug: String): Future[Option[Post]] = {
    val postContentFile = new File(postsdir + "/" + slug + "/Post.md")
    val postMetadataFile = new File(postsdir + "/" + slug + "/metadata.json")


    val g = RunnableGraph.fromGraph(GraphDSL.create(Sink.head[Option[Post]]) { implicit builder =>
      sink =>
        import GraphDSL.Implicits._



        val postcontentsource = builder.add(FileIO.fromFile(postContentFile).via(concatenator))

        val metadatasource = builder.add(FileIO.fromFile(postMetadataFile).via(concatenator).map(_.parseJson.convertTo[PostMetadata]))

        val mergemetadataanddata = builder.add(ZipWith[PostMetadata, String, Option[Post]]((metadata, postcontent) => {
          Some(Post(name = metadata.title,content = postcontent, slug = slug))
        }))



        metadatasource ~> mergemetadataanddata.in0
        postcontentsource ~> mergemetadataanddata.in1
        mergemetadataanddata.out ~> sink.in
        ClosedShape
    })


    g.run()
  }

  def getPosts(offset: Int = 0, limit: Int = 10, sortBy: (PostMetadata, PostMetadata) => Boolean = PostMetadatas.sortByCreationDate, filterBy: (PostMetadata) => Boolean = PostMetadatas.filterGetAll) : Unit = {
    //Flow[Int].drop(offset).limit(limit)

    val pairUpWithToString =
      GraphDSL.create() { implicit b =>
        import GraphDSL.Implicits._

        val byteStringFlow = b.add(Flow[PostMetadata]
          .grouped(Int.MaxValue).map(metaseq => metaseq.filter(filterBy).sortWith(sortBy))
          .drop(offset)
          .limit(limit).named("concat"))

        byteStringFlow

        FlowShape(byteStringFlow.in, b.materializedValue)
      }

    val postsiterator = (new File(postsdir + "")).listFiles.toIterator





        val postssource = Source.fromIterator(() => postsiterator).filter(_.isDirectory)

    /* val metadatasource = Flow[File]
          .map(f => FileIO.fromFile(
            new File(f.getPath + "/metadata.json"))
            .via(concatenator)
            .map(_.parseJson.convertTo[PostMetadata])) */
        val byteStringFlow = concatenator.map(f => f.parseJson.convertTo[PostMetadata]).named("myname")

    val metadatasorted = Flow[PostMetadata]
      .grouped(Int.MaxValue).map(metaseq => metaseq.filter(filterBy).sortWith(sortBy))
      .drop(offset)
      .limit(limit).named("concat")

        val flowFile2Metadata = Flow[File].map(
          f => FileIO.fromFile(new File(f.getPath + "/metadata.json")).via(byteStringFlow))








        postssource.via(flowFile2Metadata).via(pairUpWithToString).toMat



  }

*/
}
