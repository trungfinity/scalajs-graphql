package ngthanhtrung.scalajs.noton

import scala.language.experimental.macros

package object generic {
  def deriveEncoder[A]: Encoder[A] = macro DerivationMacros.encoder[A]
  def deriveDecoder[A]: Decoder[A] = macro DerivationMacros.decoder[A]
}
