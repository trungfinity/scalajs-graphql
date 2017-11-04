// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.fetch

import anduin.scalajs.io.implicits._
import cats.effect.IO

import scala.scalajs.js

trait Fetch {

  type Raw <: internal.Fetch
  val raw: Raw

  def apply(url: String): IO[js.Any] = raw.fetch(url, js.undefined).toCatsIO
  def apply(url: String, init: js.Any): IO[js.Any] = raw.fetch(url, init).toCatsIO
}
