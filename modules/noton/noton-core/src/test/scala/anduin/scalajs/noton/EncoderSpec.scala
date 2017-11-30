// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.noton

import scala.scalajs.js

import org.scalatest.{EitherValues, FlatSpec, Matchers}

final class EncoderSpec extends FlatSpec with Matchers with EitherValues {

  behavior of "Encoder"

  it should "encode integers correctly" in {
    Encoder[Int].apply(10) should be(10) // scalastyle:ignore magic.number
  }

  it should "encode strings correctly" in {
    Encoder[String].apply("hello world") should be("hello world")
  }

  it should "encode options correctly" in {
    Encoder[Option[Int]].apply(Some(10)) should be(10) // scalastyle:ignore magic.number
    Encoder[Option[Int]].apply(None) should be(null) // scalastyle:ignore null
  }

  it should "encode lists correctly" in {
    val firstArray = Encoder[List[Int]].apply(List(1, 2))
    js.Array.isArray(firstArray) should be(true)
    firstArray.asInstanceOf[js.Array[Int]].toList should be(List(1, 2)) // scalastyle:ignore token

    val secondArray = Encoder[List[Int]].apply(List())
    js.Array.isArray(secondArray) should be(true)
    secondArray.asInstanceOf[js.Array[Int]].toList should be(empty) // scalastyle:ignore token
  }
}
