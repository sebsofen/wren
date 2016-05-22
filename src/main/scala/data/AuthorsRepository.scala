package data

import java.io.File

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Flow, Source}
import com.typesafe.config.Config
import model.Authors.Author
import rest.BlogJsonSupport
import spray.json._

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by sebastian on 18/05/16.
  */
class AuthorsRepository(repodir:String)(implicit  materializer :ActorMaterializer, ec: ExecutionContext) {

  def getAuthorByName(name:String) : Future[Author] =
    Source.single(name).via(strToAuthorFlow).runWith(Sink.head)

  def strToAuthorFlow() = Flow[String].map(
    f => scala.io.Source.fromFile(repodir + "/" + f + "/author.json").mkString.parseJson
      .convertTo[Author]
  )

  def getAuthors() = Source.fromIterator(() => new File(repodir).listFiles.filter(_.isDirectory).toIterator)
    .map(f => f.getName)
    .via(strToAuthorFlow)

}
