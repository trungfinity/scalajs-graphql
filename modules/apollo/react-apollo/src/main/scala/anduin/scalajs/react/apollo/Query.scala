// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo

import scala.scalajs.js

trait Query {
  type Variables
  type Data
  val raw: internal.Query[js.Object, js.Object]
}

object Query {

  type Aux[Variables0, Data0] = Query { // scalastyle:ignore structural.type
    type Variables = Variables0
    type Data = Data0
  }
}
