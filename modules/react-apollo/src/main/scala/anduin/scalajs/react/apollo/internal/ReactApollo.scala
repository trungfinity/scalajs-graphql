// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo.internal

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("react-apollo", JSImport.Namespace, "ReactApollo")
@js.native
object ReactApollo extends js.Object {
  def graphql(query: Query): js.Function1[js.Any, js.Any] = js.native
  def renderToStringWithData(component: js.Any): js.Promise[js.Any] = js.native
}
