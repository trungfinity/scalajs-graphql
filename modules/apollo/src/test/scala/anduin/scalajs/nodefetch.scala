package anduin.scalajs

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}

@JSImport("node-fetch", JSImport.Namespace)
@js.native
object nodefetch extends js.Object {
  @JSName("default") val fetch: js.Any = js.native
}
