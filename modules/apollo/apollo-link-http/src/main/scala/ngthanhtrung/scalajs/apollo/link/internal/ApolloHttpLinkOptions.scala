package ngthanhtrung.scalajs.apollo.link.internal

import scala.scalajs.js

import ngthanhtrung.scalajs.fetch.internal.FetchFn

final class ApolloHttpLinkOptions(
  val uri: String,
  val fetch: FetchFn
) extends js.Object
