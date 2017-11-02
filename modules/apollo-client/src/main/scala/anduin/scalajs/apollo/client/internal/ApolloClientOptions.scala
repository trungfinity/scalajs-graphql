package anduin.scalajs.apollo.client.internal

import anduin.scalajs.apollo.cache.internal.ApolloCache
import anduin.scalajs.apollo.link.internal.ApolloLink

import scala.scalajs.js

final class ApolloClientOptions(
  val link: ApolloLink,
  val cache: ApolloCache
) extends js.Object
