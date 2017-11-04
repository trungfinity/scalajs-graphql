// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.apollo.link.internal

import scala.scalajs.js

import anduin.scalajs.fetch.internal.FetchFn

final class ApolloHttpLinkOptions(
  val uri: String,
  val fetch: FetchFn
) extends js.Object
