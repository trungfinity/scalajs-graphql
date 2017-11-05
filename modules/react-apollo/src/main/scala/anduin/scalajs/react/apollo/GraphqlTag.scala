// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo

import scala.scalajs.js

object GraphqlTag {

  def gql[Vars <: js.Object, Data <: js.Object](queryString: String): Query[Vars, Data] = {
    Query(internal.GraphqlTag.gql[Vars, Data](queryString))
  }
}
