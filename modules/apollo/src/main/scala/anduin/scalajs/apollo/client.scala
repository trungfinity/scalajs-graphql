package anduin.scalajs.apollo

import anduin.scalajs.apollo
import anduin.scalajs.apollo.cache.ApolloCache
import anduin.scalajs.apollo.link.ApolloLink

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object client {

  object raw {

    final class ApolloClientOptions(
      val link: apollo.link.raw.ApolloLink,
      val cache: apollo.cache.raw.ApolloCache
    ) extends js.Object

    @JSImport("apollo-client", "ApolloClient", "ApolloClient.ApolloClient")
    @js.native
    class ApolloClient(options: ApolloClientOptions) extends js.Object
  }

  final case class ApolloClientOptions(
    link: ApolloLink,
    cache: ApolloCache
  )

  final class ApolloClient(options: ApolloClientOptions) {

    def this(
      link: ApolloLink,
      cache: ApolloCache
    ) = {
      this(ApolloClientOptions(
        link,
        cache
      ))
    }

    val raw = new client.raw.ApolloClient(
      new client.raw.ApolloClientOptions(
        options.link.raw,
        options.cache.raw
      )
    )
  }
}
