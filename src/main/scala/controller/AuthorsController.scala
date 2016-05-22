package controller

import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import data.AuthorsRepository

import scala.concurrent.ExecutionContext

/**
  * Created by sebastian on 18/05/16.
  */
class AuthorsController(pr : AuthorsRepository)(implicit config: Config, materializer :ActorMaterializer, ec: ExecutionContext) {

  def getAuthorByName(name: String) = pr.getAuthorByName(name)

  def getAuthors() = pr.getAuthors

}
