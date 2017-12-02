// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo

import scala.language.higherKinds
import scala.scalajs.js

import anduin.scalajs.noton.{Decoder, Encoder}

object ReactApollo {

  def graphql[Variables, Data](
    query: Query.Aux[Variables, Data]
  )(
    implicit encoder: Encoder[Variables],
    decoder: Decoder[Data]
  ): HigherOrderComponent[Variables, ApolloQueryProps[Data]] = {
    new HigherOrderComponent[Variables, ApolloQueryProps[Data]](
      internal.ReactApollo
        .graphql(query.raw)
        // scalastyle:off token
        .asInstanceOf[internal.HigherOrderComponent[js.Object, js.Object]] // scalastyle:on token
    )
  }
}
