// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.apollo.client

import anduin.scalajs.apollo.cache.ApolloInMemoryCache
import anduin.scalajs.apollo.link.ApolloHttpLink
import anduin.scalajs.nodefetch.NodeFetch
import org.scalatest.{FlatSpec, Matchers}

final class ApolloClientSpec extends FlatSpec with Matchers {

  behavior of "Apollo client"

  it should "be created successfully" in {
    val client = new ApolloClient(
      link = new ApolloHttpLink(
        uri = "/graphql",
        fetch = NodeFetch
      ),
      cache = new ApolloInMemoryCache()
    )

    client should not be null
  }
}
