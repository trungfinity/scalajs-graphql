package anduin.scalajs

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}

object nodefetch {

  object raw {

    @JSImport("node-fetch", JSImport.Namespace)
    @js.native
    object NodeFetch extends fetch.raw.Fetch {
      @JSName(JSImport.Default) val fetch: anduin.scalajs.fetch.raw.FetchFn = js.native
    }
  }

  object NodeFetch extends fetch.Fetch {
    type Raw = nodefetch.raw.NodeFetch.type
    val raw: Raw = nodefetch.raw.NodeFetch
  }
}
