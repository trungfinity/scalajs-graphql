package anduin.scalajs

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}
import scala.scalajs.js.|

object fetch {

  object raw {

    type FetchFn = js.Function2[String | js.Any, js.UndefOr[js.Any], js.Promise[js.Any]]

    trait Fetch extends js.Object {
      @JSName(JSImport.Default) val apply: FetchFn
    }
  }

  trait Fetch {

    type Raw <: fetch.raw.Fetch
    val raw: Raw

    def apply(url: String): js.Promise[js.Any] = raw.apply(url, js.undefined)
    def apply(url: String, init: js.Any): js.Promise[js.Any] = raw.apply(url, init)
  }
}
