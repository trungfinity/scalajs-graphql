package ngthanhtrung.scalajs.apollo.cache

trait ApolloCache {
  type Raw <: internal.ApolloCache
  val raw: Raw
}
