package anduin.scalajs

import anduin.scalajs.io.implicits._
import cats.effect.IO

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}
import scala.scalajs.js.|

object fetch {

  object raw {

    type FetchFn = js.Function2[String | js.Any, js.UndefOr[js.Any], js.Promise[js.Any]]

    trait Fetch extends js.Object {
      @JSName(JSImport.Default) val fetch: FetchFn
    }
  }

  trait Fetch {

    type Raw <: fetch.raw.Fetch
    val raw: Raw

    def apply(url: String): IO[js.Any] = raw.fetch(url, js.undefined).toCatsIO
    def apply(url: String, init: js.Any): IO[js.Any] = raw.fetch(url, init).toCatsIO
  }
}
