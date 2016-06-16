package data

import java.io.File

import akka.http.scaladsl.model.DateTime
import akka.stream.scaladsl._
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import model.Posts
import model.Posts._
import rest.PostMarshalSupport
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Codec



class PostsRepository(blogname:String, repodir:String)(implicit config: Config,  materializer :ActorMaterializer, ec: ExecutionContext) extends PostMarshalSupport{
  implicit val codec = Codec("UTF-8")

  def getPostBySlug(slug: String) = getPosts(1,0,false,filterBy = Posts.filterBySlug(slug)).map(_.head)

  def getPosts(
                limit: Int,
                offset: Int,
                compact: Boolean,
                sortBy: (PostMetadata,PostMetadata) => Boolean = Posts.orderByDate,
                filterBy: PostAsm => Boolean = Posts.filterGetAll,
                reverse: Boolean = false
              ) =
    getPostMetadatasUnorderedSource()
    .grouped(Int.MaxValue)
    .map(if (reverse) _.sortWith(sortBy) else _.sortWith(sortBy).reverse)
    .map(f => f.map(f =>PostAsm(f,
      Post(
        if(compact) scala.io.Source.fromFile(repodir + "/" + f.slug + "/Post.md").mkString.split("\n\n")(0)
        else scala.io.Source.fromFile(repodir + "/" + f.slug + "/Post.md").mkString
      ))).map(replaceIncludes)
    )

    .map(f => f.filter(filterBy))
    .map(_.drop(offset).take(limit))
    .runWith(Sink.head)


  /**
    * source for posts metadata
 *
    * @return
    */
  def getPostMetadatasUnorderedSource() = Source.fromIterator(() => new File(repodir).listFiles.filter(_.isDirectory).toIterator)
    .map(f => f.getName)
    .via(slugToMetadata)



  /**
    * from a slug, read metadata
    *
    * @return
    */
  def slugToMetadata()  = Flow[String].map(f => scala.io.Source.fromFile(repodir + "/" + f + "/metadata.json").mkString.parseJson.convertTo[PostMetadata].copy(slug=f))

  def assemblePostFromMetadata() = Flow[PostMetadata].map(f => PostAsm(f,Post(scala.io.Source.fromFile(repodir + "/" + f.slug + "/Post.md").mkString)))

  /**
    *
    * @return
    */
  def replaceIncludes : PostAsm => PostAsm = f => {
    f.copy(post = f.post.copy(content = replaceFileInclude(f.post.content,f.metadata.slug)))
  }

  /**
    * find include file statements and replace with file contents: cyclic dependencies will create an overflow!
    *
    * @param content
    * @param slug
    * @return
    */
  def replaceFileInclude(content: String, slug:String) : String = "include file=\\\"(.*)\\\"".r.replaceAllIn(content, m => {
  //def replaceFileInclude(content: String, slug:String) : String = """"(.*)"""".r.replaceAllIn(content, m => {
    val filename = m.group(1)
        replaceFileInclude(scala.io.Source.fromFile(repodir + "/" + replaceTildeWithSlugPath(filename,slug)).mkString, slug)
        //content
      })


  def getFeed() : Future[Feed] = {
    getPosts(10,0,true,Posts.orderByDate,Posts.filterGetAll,false)
      .flatMap(psts => Future(Feed(FeedMeta(
        blogname = blogname,
        updated = DateTime(psts.head.metadata.created),
        blogurl = config.getString("blogs." + blogname + ".blogurl" ),
        id = "id",
        author = "author",
        postsUrlPref = config.getString("blogs." + blogname + ".blogurl" )
      ),psts)))
  }


  /**
    * find ~ character and replace with current post slug.
 *
    * @param content
    * @param slug
    * @return
    */
  def replaceTildeWithSlugPath(content: String, slug:String) = """~""".r.replaceFirstIn(content,slug)




}
