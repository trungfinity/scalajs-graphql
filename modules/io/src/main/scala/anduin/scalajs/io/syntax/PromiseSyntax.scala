// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.io.syntax

import cats.effect.IO

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

trait PromiseSyntax {

  implicit final def asjsPromiseSyntax[A](promise: js.Promise[A]): PromiseOps[A] = {
    new PromiseOps[A](promise)
  }
}

final class PromiseOps[A](val promise: js.Promise[A]) extends AnyVal {

  def toCatsIO: IO[A] = {
    IO.async { callback =>
      promise.then[Unit](
        { value =>
          callback(Right(value))
        },
        js.defined { exception =>
          callback(Left(exception match {
            case throwable: Throwable => throwable
            case _ => JavaScriptException(exception)
          }))
        }
      )
    }
  }
}
