package anduin.scalajs.apollo.cache

import scala.scalajs.js

final class ApolloInMemoryCache(options: ApolloInMemoryCacheOptions) extends ApolloCache {

  def this(
    addTypename: js.UndefOr[Boolean] = js.undefined
  ) = {
    this(ApolloInMemoryCacheOptions(
      addTypename
    ))
  }

  type Raw = internal.ApolloInMemoryCache

  val raw = new internal.ApolloInMemoryCache(
    new internal.ApolloInMemoryCacheOptions(
      options.addTypename
    )
  )
}
