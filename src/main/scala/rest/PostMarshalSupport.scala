package rest

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
  * Created by sebastian on 27/04/16.
  */
trait PostMarshalSupport extends SprayJsonSupport with ScalaXmlSupport with DefaultJsonProtocol {
  implicit val postMetadataFormat = jsonFormat6(PostMetadata)
  implicit val postFormat = jsonFormat1(Post)
  implicit val postAsmFormat = jsonFormat2(PostAsm)
  implicit val blogMetaInfo = jsonFormat4(BlogMetaInfo)



  def marshalPost(obj: PostAsm) : NodeSeq = <title>{obj.metadata.title}</title>

  def marshalPosts(posts: Iterable[PostAsm]): NodeSeq = <posts>{posts.map(marshalPost)}</posts>

  implicit def postsXmlFormat = Marshaller.opaque[Iterable[PostAsm],NodeSeq](marshalPosts)
  implicit def catalogItemXmlFormat = Marshaller.opaque[PostAsm, NodeSeq](marshalPost)



  def feedXml(feed:Future[Feed]) : Future[NodeSeq] = feed.map(feed =>
      <feed xmlns="http://www.w3.org/2005/Atom">
      <updated>{feed.meta.updated.toIsoDateTimeString()}</updated>
      <title>{feed.meta.blogname}</title>
      <link href={feed.meta.blogurl}/>
        <author>
        <name>{feed.meta.author}</name>
        </author>
        <id>{feed.meta.id}</id>

        <!-- entries follwo here: -->
        {feed.posts.map((f:PostAsm) => entryXml(f,feed))}
      </feed>
  )

  def entryXml(post:PostAsm, feed: Feed) : NodeSeq =
    <entry>
      <title>{post.metadata.title}</title>
      <link href={feed.meta.postsUrlPref + "/" +post.metadata.slug} />
      <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>
      <updated>{DateTime(post.metadata.created * 100l).toIsoDateTimeString() }</updated>
      <summary>{post.post.content}</summary>
    </entry>

  val xmlFeedMarshaller: ToEntityMarshaller[Future[Feed]] = Marshaller.combined(feedXml)

  implicit val feedMarchaller: ToResponseMarshaller[Future[Feed]] = Marshaller.oneOf(xmlFeedMarshaller)




}

