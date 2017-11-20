// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.noton

import scala.language.experimental.macros

package object generic {
  def deriveDecoder[A]: Decoder[A] = macro DerivationMacros.decoder[A]
}
