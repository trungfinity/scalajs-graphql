// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo.internal

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}

@JSImport("graphql-tag", JSImport.Namespace, "GraphqlTag")
@js.native
object GraphqlTag extends js.Object {

  @JSName(JSImport.Default) def gql[Vars <: js.Object, Data <: js.Object](
    queryString: String
  ): Query[Vars, Data] = js.native
}
