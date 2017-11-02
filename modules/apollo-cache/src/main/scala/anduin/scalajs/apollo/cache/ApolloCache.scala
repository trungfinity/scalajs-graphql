package anduin.scalajs.apollo.cache

trait ApolloCache {
  type Raw <: internal.ApolloCache
  val raw: Raw
}
