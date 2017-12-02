// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo

import scala.scalajs.js

import anduin.scalajs.noton.Decoder
import anduin.scalajs.noton.generic.deriveDecoder

final case class ApolloQueryProps[Data](
  data: Option[Data],
  loading: Boolean
)(
  raw: js.Any
)

object ApolloQueryProps {

  implicit def decoder[Data: Decoder]: Decoder[ApolloQueryProps[Data]] = {
    deriveDecoder[ApolloQueryProps[Data]]
  }
}
