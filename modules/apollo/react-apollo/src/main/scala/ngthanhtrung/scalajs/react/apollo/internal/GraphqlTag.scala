// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.scalajs.react.apollo.internal

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}

@JSImport("graphql-tag", JSImport.Namespace, "GraphqlTag")
@js.native
object GraphqlTag extends js.Object {

  @JSName(JSImport.Default) def gql[Variables <: js.Any, Data <: js.Any](
    queryString: String
  ): Query[Variables, Data] = js.native
}
