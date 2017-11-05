// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo.internal

import scala.scalajs.js

trait ApolloQueryProps[Data] extends js.Object {
  val data: js.UndefOr[Data]
  val loading: Boolean
}
