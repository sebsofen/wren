import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * Created by sebastian on 11/04/16.
  */


case class Post(content: String)

case class PostMetadata(title:String, created: Long, tags: Seq[String] = Seq(), slug: String )

case class PostAsm(metadata: PostMetadata,post: Post)


trait PostJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val postMetadataFormat = jsonFormat4(PostMetadata)
  implicit val postFormat = jsonFormat1(Post)
  implicit val postAsmFormat = jsonFormat2(PostAsm)
}


