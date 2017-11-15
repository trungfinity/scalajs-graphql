// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo.internal

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

// scalastyle:off underscore.import
import japgolly.scalajs.react.raw._
// scalastyle:on underscore.import

@JSImport("react-apollo", JSImport.Namespace, "ReactApollo")
@js.native
object ReactApollo extends js.Object {

  def graphql[Vars <: js.Object, Data <: js.Object](
    query: Query[Vars, Data]
  ): HigherOrderComponent[Vars, ApolloQueryProps[Data]] = js.native

  def getDataFromTree(component: ReactElement): js.Promise[Undefined] = js.native
  def renderToStringWithData(component: ReactElement): js.Promise[String] = js.native
}
