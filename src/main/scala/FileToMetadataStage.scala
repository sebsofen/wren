import java.io.File

import akka.stream.scaladsl.{Sink, FileIO}
import akka.stream._
import akka.stream.stage.{OutHandler, InHandler, GraphStageLogic, GraphStage}
import api.PostMetadata
import com.typesafe.config.Config
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.{Future, ExecutionContext}


/**
  * Created by sebastian on 27/03/16.

class FileToMetadataStage(implicit config: Config,  materializer :ActorMaterializer, ec: ExecutionContext) extends GraphStage[FlowShape[File,Future[PostMetadata]]] {
  implicit val personFormat: JsonReader[PostMetadata] = jsonFormat4(PostMetadata)
  val in = Inlet[File]("filein")
  val out = Outlet[Future[PostMetadata]]("metadataout")
  override def shape: FlowShape[File, Future[PostMetadata]] = FlowShape.of(in,out)

  implicit val mata  = materializer

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    setHandler(in,new InHandler {

      override def onPush(): Unit = {

        val element = grab(in)

        val metadataSource = FileIO.fromFile(new File(element.getPath + "/metadata.json"))
          .map(_.utf8String)
          .grouped(Int.MaxValue).map(f => f.mkString)
          .map(f => f.parseJson.convertTo[PostMetadata]).to(Sink.head[PostMetadata])

        push(out,metadataSource)

      }
      //override def onUpstreamFinish() : Unit = {
      //  if(stageelement.isDefined) emit(out,stageelement.get)
      //  complete(out)
      //}
    }


    )

    setHandler(out,new OutHandler {
      override def onPull(): Unit = {
        pull(in)


      }

    })
  }


}
  */