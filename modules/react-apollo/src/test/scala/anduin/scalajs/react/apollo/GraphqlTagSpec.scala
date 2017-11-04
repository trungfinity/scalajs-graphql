package anduin.scalajs.react.apollo

import org.scalatest.{FlatSpec, Matchers}

final class GraphqlTagSpec extends FlatSpec with Matchers {

  behavior of "GraphQL tag"

  it should "parse query successfully" in {
    GraphqlTag.gql[Null, Null]("query { name }") should not be null
  }
}
