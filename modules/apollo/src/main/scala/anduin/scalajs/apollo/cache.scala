package anduin.scalajs.apollo

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object cache {

  private[apollo] object raw {

    @JSImport("apollo-cache", "ApolloCache", "ApolloCache.ApolloCache")
    @js.native
    class ApolloCache extends js.Object

    final class ApolloInMemoryCacheOptions(
      val addTypename: js.UndefOr[Boolean]
    ) extends js.Object

    @JSImport("apollo-cache-inmemory", "InMemoryCache", "ApolloCacheInmemory.InMemoryCache")
    @js.native
    class ApolloInMemoryCache(options: ApolloInMemoryCacheOptions) extends ApolloCache
  }

  trait ApolloCache {
    private[apollo] type Raw <: cache.raw.ApolloCache
    private[apollo] val raw: Raw
  }

  final case class ApolloInMemoryCacheOptions(
    addTypename: js.UndefOr[Boolean] = js.undefined
  )

  final class ApolloInMemoryCache(options: ApolloInMemoryCacheOptions) extends ApolloCache {

    def this(
      addTypename: js.UndefOr[Boolean] = js.undefined
    ) = {
      this(ApolloInMemoryCacheOptions(
        addTypename
      ))
    }

    private[apollo] type Raw = cache.raw.ApolloInMemoryCache

    private[apollo] val raw = new cache.raw.ApolloInMemoryCache(
      new cache.raw.ApolloInMemoryCacheOptions(
        options.addTypename
      )
    )
  }
}
