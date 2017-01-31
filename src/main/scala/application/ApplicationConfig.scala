package application

import com.typesafe.config.{Config}

import scala.collection.JavaConversions._ // mandatory for getKey stuff
/**
  * Created by sebastian on 10/3/16.
  */
class ApplicationConfig(config: Config) {

  lazy val BLOGSPECS: Seq[Blog] = (for {
    entry <- config.getObject("blogs").entrySet
    blogname = entry.getKey
    postsrepo = entry.getValue.atKey(blogname).getString(blogname + ".posts")
    authorsrepo = entry.getValue
      .atKey(blogname)
      .getString(blogname + ".authors")
    guifiles = entry.getValue.atKey(blogname).getString(blogname + ".guifiles")
    blogurl = entry.getValue.atKey(blogname).getString(blogname + ".blogurl")

    port = if (entry.getValue.atKey(blogname).hasPath(blogname + ".port"))
      entry.getValue.atKey(blogname).getInt(blogname + ".port")
    else DEFAULT_INTERFACE.port

    interface = if (entry.getValue.atKey(blogname).hasPath(blogname + ".interface"))
      entry.getValue.atKey(blogname).getString(blogname + ".interface")
    else DEFAULT_INTERFACE.interface

    interfaceport: InterfacePort = InterfacePort(interface, port)

  } yield
    Blog(blogname,
         postsrepo,
         authorsrepo,
         guifiles,
         blogurl,
         blogname == DEFAULTBLOG,
         interfaceport)).toSeq

  /**
    * get default blog if set in configuration, otherwise get first blog in blog list
    */
  lazy val DEFAULTBLOG: String =
    if (config.hasPath("blogdefault"))
      config.getString("blogdefault")
    else
      config.getObject("blogs").entrySet.iterator.next.getKey

  lazy val DEFAULT_INTERFACE: InterfacePort = InterfacePort(
    config.getString("http.interface"),
    config.getInt("http.port"))

}

case class InterfacePort(interface: String, port: Int)
case class Blog(name: String,
                postrepo: String,
                authorsrepo: String,
                guiRepo: String,
                blogurl: String,
                default: Boolean,
                interfacePort: InterfacePort)
