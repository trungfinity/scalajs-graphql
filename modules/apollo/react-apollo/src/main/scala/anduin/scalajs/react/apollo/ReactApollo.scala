// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo

import scala.language.higherKinds
import scala.scalajs.js

import anduin.scalajs.react.apollo.internal.{ApolloQueryProps, HigherOrderComponent}

object ReactApollo {

  def graphql[Vars <: js.Object, Data <: js.Object](
    query: Query[Vars, Data]
  ): HigherOrderComponent[Vars, ApolloQueryProps[Data]] = {
    internal.ReactApollo.graphql(query.raw)
  }
}
