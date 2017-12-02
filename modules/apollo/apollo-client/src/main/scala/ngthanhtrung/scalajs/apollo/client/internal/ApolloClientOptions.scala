package ngthanhtrung.scalajs.apollo.client.internal

import scala.scalajs.js

import ngthanhtrung.scalajs.apollo.cache.internal.ApolloCache
import ngthanhtrung.scalajs.apollo.link.internal.ApolloLink

final class ApolloClientOptions(
  val link: ApolloLink,
  val cache: ApolloCache,
  val ssrMode: js.UndefOr[Boolean] = js.undefined
) extends js.Object
