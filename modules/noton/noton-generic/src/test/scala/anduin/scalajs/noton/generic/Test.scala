// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.noton.generic

import scala.language.experimental.macros
import scala.scalajs.js

import anduin.scalajs.noton.Decoder

object Test {

  final case class Hello(a: Int, b: String)(raw: js.Any)
  final case class Foo()(raw: js.Any)

  def deriveDecoder[T]: Decoder[T] = macro DerivationMacros.decoder[T]

  def main(args: Array[String]): Unit = {
    println(deriveDecoder[Foo](js.Dynamic.literal()))
    println(deriveDecoder[Hello](js.Dynamic.literal("a" -> 122, "b" -> "hello")))
  }
}
