// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.apollo.cache

import scala.scalajs.js

final case class ApolloInMemoryCacheOptions(
  addTypename: js.UndefOr[Boolean] = js.undefined
)
