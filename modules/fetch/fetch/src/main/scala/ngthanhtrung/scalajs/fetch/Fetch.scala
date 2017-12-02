// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.scalajs.fetch

import scala.scalajs.js

import cats.effect.IO

// scalastyle:off underscore.import
import ngthanhtrung.scalajs.io.implicits._
// scalastyle:on underscore.import

trait Fetch {

  type Raw <: internal.Fetch
  val raw: Raw

  def apply(url: String): IO[js.Any] = raw.fetch(url, js.undefined).toCatsIO
  def apply(url: String, init: js.Any): IO[js.Any] = raw.fetch(url, init).toCatsIO
}
