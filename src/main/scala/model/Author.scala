package model

/**
  * Created by sebastian on 9/22/16.
  */
object Author {
  case class Author(id: Option[String],name: String, img: String, desc: String, title: String)
}
