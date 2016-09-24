package data

import java.io.File

import akka.stream.scaladsl.{Sink, Source}
import model.Author
import model.Posts.PostMetadata
import rest.AuthorMarshalSupport
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by sebastian on 18/05/16.
  */
class AuthorsRepository(blogname: String, repdir: String)(implicit materializer: ActorMaterializer,
                                                          ec: ExecutionContext)
    extends RepositoryTrait
    with AuthorMarshalSupport {
  override val repodir: String = repdir
  def getAuthorByNames(name: Seq[String]): Future[Seq[Author.Author]] =
    getAuthorNamesList().filter(name.contains(_)).map(name2Author).runWith(Sink.seq)

  private def getAuthorNamesList() =
    Source
      .fromIterator(() => new File(repodir).listFiles.filter(_.isDirectory).toIterator)
      .map(f => {
        f.getName
      })
      .filter(f => getWrenIgnore.map(pat => f.matches(pat)).count(_ == true) == 0)

  def getAuthorsList() = getAuthorNamesList().map(name2Author).runWith(Sink.seq)

  def name2Author(name: String): Author.Author =
    scala.io.Source
      .fromFile(repodir + "/" + name + "/metadata.json")
      .mkString
      .parseJson
      .convertTo[Author.Author]
      .copy(id = Some(name))

}
