// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo

import scala.language.higherKinds
import scala.scalajs.js

// scalastyle:off underscore.import
import japgolly.scalajs.react._
// scalastyle:on underscore.import

object ReactApollo {

  def graphql[P <: js.Object, V <: js.Object, CT[-p, +u] <: CtorType[p, u]](
    query: Query[P, V]
  ): JsComponent[P, _, CT] => JsComponent[V, _, CT] = {
    internal.ReactApollo
      .graphql(query.raw)
      .asInstanceOf[JsComponent[P, _, CT] => JsComponent[V, _, CT]] // scalastyle:ignore token
  }
}
