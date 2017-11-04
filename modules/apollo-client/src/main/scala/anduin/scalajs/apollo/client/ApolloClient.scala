package anduin.scalajs.apollo.client

import anduin.scalajs.apollo.cache.ApolloCache
import anduin.scalajs.apollo.link.ApolloLink

import scala.scalajs.js

final class ApolloClient(options: ApolloClientOptions) {

  def this(
    link: ApolloLink,
    cache: ApolloCache,
    ssrMode: js.UndefOr[Boolean] = js.undefined
  ) = {
    this(ApolloClientOptions(
      link,
      cache,
      ssrMode
    ))
  }

  val raw = new internal.ApolloClient(
    new internal.ApolloClientOptions(
      options.link.raw,
      options.cache.raw,
      options.ssrMode
    )
  )
}
