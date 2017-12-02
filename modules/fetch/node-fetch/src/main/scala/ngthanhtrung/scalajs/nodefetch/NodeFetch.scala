package ngthanhtrung.scalajs.nodefetch

import ngthanhtrung.scalajs.fetch.Fetch

object NodeFetch extends Fetch {
  type Raw = internal.NodeFetch.type
  val raw: Raw = internal.NodeFetch
}
