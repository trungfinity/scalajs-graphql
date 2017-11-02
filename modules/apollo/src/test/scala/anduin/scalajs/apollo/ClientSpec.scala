package anduin.scalajs.apollo

import anduin.scalajs.apollo.cache.ApolloInMemoryCache
import anduin.scalajs.apollo.client.ApolloClient
import anduin.scalajs.apollo.link.ApolloHttpLink
import anduin.scalajs.nodefetch
import org.scalatest.{FlatSpec, Matchers}

final class ClientSpec extends FlatSpec with Matchers {

  behavior of "Apollo client"

  it should "be created successfully" in {
    val client = new ApolloClient(
      link = new ApolloHttpLink(
        uri = "/graphql",
        fetch = nodefetch.fetch
      ),
      cache = new ApolloInMemoryCache()
    )

    client should not be null
  }
}
