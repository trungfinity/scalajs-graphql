// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo.internal

import org.scalatest.{FlatSpec, Matchers}

final class GraphqlTagSpec extends FlatSpec with Matchers {

  behavior of "GraphQL tag"

  it should "parse query successfully" in {
    GraphqlTag.gql[Null, Null]("query { name }") should not be null // scalastyle:ignore null
  }
}
