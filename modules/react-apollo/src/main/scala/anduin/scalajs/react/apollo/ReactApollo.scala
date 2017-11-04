package anduin.scalajs.react.apollo

import japgolly.scalajs.react._

import scala.language.higherKinds
import scala.scalajs.js

object ReactApollo {

  def graphql[P <: js.Object, V <: js.Object, CT[-p, +u] <: CtorType[p, u]](
    query: Query[P, V]
  ): JsComponent[P, _, CT] => JsComponent[V, _, CT] = {
    internal.ReactApollo.graphql(query.raw).asInstanceOf[JsComponent[P, _, CT] => JsComponent[V, _, CT]]
  }
}
