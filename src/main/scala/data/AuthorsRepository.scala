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
    getAuthorsList()
      .filter(name.contains(_))
      .map(f =>{

        scala.io.Source.fromFile(repodir + "/" + f + "/metadata.json").mkString.parseJson.convertTo[Author.Author]
      }
        )
      .runWith(Sink.seq)

  def getAuthorsList() ={
    println(repodir)
    Source
      .fromIterator(() => new File(repodir).listFiles.filter(_.isDirectory).toIterator)
      .map(f => {
        println(f.getName)
        f.getName
      })
      .filter(f => getWrenIgnore.map(pat => f.matches(pat)).count(_ == true) == 0)

  }

}
