package anduin.scalajs.apollo.client

import anduin.scalajs.apollo.cache.ApolloCache
import anduin.scalajs.apollo.link.ApolloLink

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

  val raw = new internal.ApolloClient(
    new internal.ApolloClientOptions(
      options.link.raw,
      options.cache.raw
    )
  )
}
