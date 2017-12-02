package ngthanhtrung.scalajs.react.apollo

import scala.scalajs.js

import ngthanhtrung.scalajs.noton.Decoder
import ngthanhtrung.scalajs.noton.generic.deriveDecoder

final case class ApolloQueryProps[Data](data: Data)(raw: js.Any)

object ApolloQueryProps {

  implicit def decoder[Data: Decoder]: Decoder[ApolloQueryProps[Data]] = {
    deriveDecoder[ApolloQueryProps[Data]]
  }
}
