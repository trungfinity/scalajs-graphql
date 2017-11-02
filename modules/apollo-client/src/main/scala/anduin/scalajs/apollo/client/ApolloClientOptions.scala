package anduin.scalajs.apollo.client

import anduin.scalajs.apollo.cache.ApolloCache
import anduin.scalajs.apollo.link.ApolloLink

final case class ApolloClientOptions(
  link: ApolloLink,
  cache: ApolloCache
)
