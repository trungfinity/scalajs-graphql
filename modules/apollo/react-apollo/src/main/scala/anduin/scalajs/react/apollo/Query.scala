// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo

import scala.scalajs.js

final case class Query[Data <: js.Object, Vars <: js.Object](
  raw: internal.Query[Data, Vars]
)
