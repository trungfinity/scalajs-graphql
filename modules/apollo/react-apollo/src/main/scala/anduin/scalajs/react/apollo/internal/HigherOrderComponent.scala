// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo.internal

import scala.scalajs.js

import japgolly.scalajs.react.raw.ReactClass

@js.native
trait HigherOrderComponent[Props <: js.Object, ChildProps <: js.Object] extends js.Object {

  def apply[Extra <: js.Any](
    component: js.Any // Cannot make it more type-safe
  ): ReactClass[ChildProps with Extra, Null] = js.native
}
