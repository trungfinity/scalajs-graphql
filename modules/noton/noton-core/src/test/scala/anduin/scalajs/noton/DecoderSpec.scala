// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.noton

import scala.scalajs.js

import org.scalatest.{EitherValues, FlatSpec, Matchers}

final class DecoderSpec extends FlatSpec with Matchers with EitherValues {

  behavior of "Decoder"

  it should "decode integers correctly" in {
    Decoder[Int].apply(10).right.value should be(10) // scalastyle:ignore magic.number
    // Decoder[Int].apply(10.4).left.value
  }

  it should "decode strings correctly" in {
    Decoder[String].apply("hello world").right.value should be("hello world")
  }

  it should "decode options correctly" in {
    Decoder[Option[Int]].apply(10).right.value should be(Some(10)) // scalastyle:ignore magic.number
    Decoder[Option[Int]].apply(null).right.value should be(None) // scalastyle:ignore null
    Decoder[Option[Int]].apply(js.undefined).right.value should be(None)
  }

  it should "decode lists correctly" in {
    Decoder[List[Int]].apply(js.Array(1)).right.value should be(List(1))
    Decoder[List[Int]].apply(1).left.value
    Decoder[List[Int]].apply(null).left.value // scalastyle:ignore null
    Decoder[List[Int]].apply(js.undefined).left.value
  }
}
