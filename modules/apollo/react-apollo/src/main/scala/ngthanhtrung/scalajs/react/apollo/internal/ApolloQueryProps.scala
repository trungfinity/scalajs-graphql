// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.scalajs.react.apollo.internal

import scala.scalajs.js

trait ApolloQueryProps[Data <: js.Object] extends js.Object {
  val data: js.UndefOr[Data]
  val loading: Boolean
}
