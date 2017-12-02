package ngthanhtrung.scalajs.apollo.client

import scala.scalajs.js

import ngthanhtrung.scalajs.apollo.cache.ApolloCache
import ngthanhtrung.scalajs.apollo.link.ApolloLink

final case class ApolloClientOptions(
  link: ApolloLink,
  cache: ApolloCache,
  ssrMode: js.UndefOr[Boolean]
)
