package anduin.scalajs.nodefetch

import anduin.scalajs.fetch.Fetch

object NodeFetch extends Fetch {
  type Raw = internal.NodeFetch.type
  val raw: Raw = internal.NodeFetch
}
