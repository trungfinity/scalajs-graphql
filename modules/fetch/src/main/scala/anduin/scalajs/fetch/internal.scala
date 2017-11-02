package anduin.scalajs.fetch

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}
import scala.scalajs.js.|

object internal {

  type FetchFn = js.Function2[String | js.Any, js.UndefOr[js.Any], js.Promise[js.Any]]

  trait Fetch extends js.Object {
    @JSName(JSImport.Default) val fetch: FetchFn
  }
}
