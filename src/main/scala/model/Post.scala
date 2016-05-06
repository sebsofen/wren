package model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * Created by sebastian on 11/04/16.
  */

object Posts {


  case class Post(content: String)
  case class PostMetadata(title:String, created: Long, tags: Set[String] = Set(), slug: String )
  case class PostAsm(metadata: PostMetadata,post: Post)

  case class BlogMetaInfo(
                           tags: Set[String] = Set(),
                           start: Long = Long.MaxValue,
                           stop: Long = Long.MinValue,
                           postCount : Int = 0)

  def orderByDate(m1 : PostMetadata, m2: PostMetadata) = m1.created < m2.created
  def filterGetAll(m : PostAsm) = true

}




