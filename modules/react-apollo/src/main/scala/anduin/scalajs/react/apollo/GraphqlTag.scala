package anduin.scalajs.react.apollo

object GraphqlTag {

  def gql[P, V](queryString: String): Query[P, V] = {
    Query[P, V](internal.GraphqlTag.gql(queryString))
  }
}
