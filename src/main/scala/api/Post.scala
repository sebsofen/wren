package api

import scala.concurrent.Future

/**
  * Created by sebastian on 10/03/16.
  */
case class Post(content: Future[String], metadata: PostMetadata)



