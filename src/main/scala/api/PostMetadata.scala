package api

/**
  * Created by sebastian on 26/03/16.
  */
case class PostMetadata(title: String, created: Long, tags: Seq[String])


object PostMetadata  {

  def sortByCreationDate(a : PostMetadata, b : PostMetadata) = {
    a.created < b.created
  }

  def filterGetAll(a : PostMetadata) = true

  /**
    * this works as follows: we always expect a filter function to look like def blabla(x :PostMetadata) = true | false
    * that's whats happening in this function: (a: PostMetadata) => {} is the return statement, which create an anonymous? function,
    * that compares the given tags with tha tags in the metadataobject. if one tag is contained in the list => return true
    * @param tags list of tags to look for
    * @return
    */
  def filterGetTags(tags : Seq[String]) = {
    (a: PostMetadata) => {
        a.tags.intersect(tags).length > 0
    }
  }
}

