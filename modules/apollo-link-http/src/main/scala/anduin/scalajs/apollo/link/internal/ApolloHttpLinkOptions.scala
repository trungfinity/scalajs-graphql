package anduin.scalajs.apollo.link.internal

import anduin.scalajs.fetch.internal.FetchFn

import scala.scalajs.js

final class ApolloHttpLinkOptions(
  val uri: String,
  val fetch: FetchFn
) extends js.Object
