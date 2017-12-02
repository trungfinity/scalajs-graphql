// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.scalajs.apollo.client

import scala.scalajs.js

import ngthanhtrung.scalajs.apollo.cache.ApolloCache
import ngthanhtrung.scalajs.apollo.link.ApolloLink

final class ApolloClient(options: ApolloClientOptions) {

  def this(
    link: ApolloLink,
    cache: ApolloCache,
    ssrMode: js.UndefOr[Boolean] = js.undefined
  ) = {
    this(
      ApolloClientOptions(
        link,
        cache,
        ssrMode
      )
    )
  }

  val raw = new internal.ApolloClient(
    new internal.ApolloClientOptions(
      options.link.raw,
      options.cache.raw,
      options.ssrMode
    )
  )
}
