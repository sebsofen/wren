package data

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import model.Posts.{PostAsm, Post, PostMetadata}
import spray.json.DefaultJsonProtocol

/**
  * Created by sebastian on 27/04/16.
  */
trait PostJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val postMetadataFormat = jsonFormat4(PostMetadata)
  implicit val postFormat = jsonFormat1(Post)
  implicit val postAsmFormat = jsonFormat2(PostAsm)
}
