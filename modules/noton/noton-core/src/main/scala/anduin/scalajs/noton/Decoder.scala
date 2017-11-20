// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.noton

import scala.scalajs.js

trait Decoder[A] {
  def apply(any: js.Any): Decoder.Result[A]
}

object Decoder {

  type Result[A] = Either[Throwable, A]

  def instance[A](f: js.Any => Result[A]): Decoder[A] = any => f(any)

  // scalastyle:off token

  implicit val intDecoder: Decoder[Int] = instance { any =>
    Right(any.asInstanceOf[Int])
  }

  implicit val stringDecoder: Decoder[String] = instance { any =>
    Right(any.asInstanceOf[String])
  }

  // scalastyle:on token

  implicit def optionDecoder[A](implicit decoder: Decoder[A]): Decoder[Option[A]] = {
    instance { any =>
      if (!js.isUndefined(any) && any != null) { // scalastyle:ignore null
        decoder(any).map(Some(_))
      } else {
        Right(None)
      }
    }
  }
}
