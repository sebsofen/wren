package controller

import akka.stream.ActorMaterializer
import application.ApplicationConfig
import com.typesafe.config.Config
import data.AuthorsRepository
import model.Author

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by sebastian on 18/05/16.
  */
class AuthorsController(
    ar: AuthorsRepository)(implicit materializer: ActorMaterializer, ec: ExecutionContext) {
  def getAuthorByName(name: Seq[String]): Future[Seq[Author.Author]] = ar.getAuthorByNames(name)
  def getAuthorsList(): Future[Seq[Author.Author]] = ar.getAuthorsList()

}
