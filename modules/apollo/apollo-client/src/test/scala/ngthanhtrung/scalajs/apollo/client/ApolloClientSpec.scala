package ngthanhtrung.scalajs.apollo.client

import org.scalatest.{FlatSpec, Matchers}

import ngthanhtrung.scalajs.apollo.cache.ApolloInMemoryCache
import ngthanhtrung.scalajs.apollo.link.ApolloHttpLink
import ngthanhtrung.scalajs.nodefetch.NodeFetch

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

    client should not be null // scalastyle:ignore null
  }
}
