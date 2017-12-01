// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.noton.generic

import scala.scalajs.js

import org.scalatest.{FlatSpec, Matchers}

final class DerivationSpec extends FlatSpec with Matchers {

  private[this] final case class Foo()(raw: js.Any)

  private[this] val fooJs = js.Dynamic.literal()
  private[this] val foo = Foo()(fooJs)

  private[this] final case class Hello(a: Int, b: String)(raw: js.Any)

  private[this] val helloJS = js.Dynamic.literal("a" -> 122, "b" -> "hello")
  private[this] val hello = Hello(122, "hello")(helloJS) // scalastyle:ignore magic.number

  private[this] final case class Something(x: Int)
  private[this] val something = Something(30) // scalastyle:ignore magic.number

  behavior of "Encoder derivation"

  it should "derive encoders correctly" in {
    js.JSON.stringify(deriveEncoder[Foo](foo)) should be("{}")
    js.JSON.stringify(deriveEncoder[Hello](hello)) should be("""{"a":122,"b":"hello"}""")
    js.JSON.stringify(deriveEncoder[Something](something)) should be("""{"x":30}""")
  }

  behavior of "Decoder derivation"

  it should "derive decoders correctly" in {
    deriveDecoder[Foo](fooJs) should be(Right(foo))
    deriveDecoder[Hello](helloJS) should be(Right(hello))
  }
}
