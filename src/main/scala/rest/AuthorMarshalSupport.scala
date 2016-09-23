package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import model.Author
import model.Posts.PostMetadata
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.{Marshal, Marshaller, ToEntityMarshaller, ToResponseMarshaller}
import scala.xml.NodeSeq
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import akka.http.scaladsl.model.DateTime
import model.Posts._
import spray.json.DefaultJsonProtocol
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
/**
  * Created by sebastian on 9/22/16.
  */
trait AuthorMarshalSupport extends SprayJsonSupport  with DefaultJsonProtocol {
  implicit val authorFormat = jsonFormat4(Author.Author)

}
