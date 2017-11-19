// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.noton

import scala.scalajs.js

trait Encoder[A] {
  def apply(a: A): js.Any
}

object Encoder
