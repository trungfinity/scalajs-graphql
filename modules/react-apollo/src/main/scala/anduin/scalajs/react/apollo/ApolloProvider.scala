// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo

import anduin.scalajs.apollo.client.ApolloClient
import anduin.scalajs.apollo.client.internal.{ApolloClient => RawApolloClient}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomNode

import scala.scalajs.js

object ApolloProvider {

  final class Props(val client: RawApolloClient) extends js.Object

  val component: JsComponent[Props, Null, CtorType.PropsAndChildren] = {
    JsComponent[Props, Children.Varargs, Null](internal.ApolloProvider)
  }

  def apply(client: ApolloClient)(child: VdomNode): JsComponent.Unmounted[Props, Null] = {
    component(new Props(client.raw))(child)
  }
}
