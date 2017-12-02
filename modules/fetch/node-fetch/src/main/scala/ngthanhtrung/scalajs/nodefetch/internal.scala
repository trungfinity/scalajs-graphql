package ngthanhtrung.scalajs.nodefetch

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}

import ngthanhtrung.scalajs.fetch.internal.{Fetch, FetchFn}

object internal {

  @JSImport("node-fetch", JSImport.Namespace)
  @js.native
  object NodeFetch extends Fetch {
    @JSName(JSImport.Default) val fetch: FetchFn = js.native
  }
}
