package ngthanhtrung.scalajs.apollo.cache.internal

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("apollo-cache-inmemory", "InMemoryCache", "ApolloCacheInmemory.InMemoryCache")
@js.native
class ApolloInMemoryCache(options: ApolloInMemoryCacheOptions) extends ApolloCache
