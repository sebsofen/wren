package data

/**
  * Created by sebastian on 9/22/16.
  */
trait RepositoryTrait {
  val repodir : String
  lazy val getWrenIgnore : Seq[String] =
    try {
      scala.io.Source.fromFile(repodir + "/" + ".wrenignore").getLines().toSeq
    } catch {
      case _ : Throwable => Seq()
    }
}
