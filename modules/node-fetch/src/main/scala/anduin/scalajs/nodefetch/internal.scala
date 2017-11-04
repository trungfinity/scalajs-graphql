// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.nodefetch

import anduin.scalajs.fetch.internal.{Fetch, FetchFn}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}

object internal {

  @JSImport("node-fetch", JSImport.Namespace)
  @js.native
  object NodeFetch extends Fetch {
    @JSName(JSImport.Default) val fetch: FetchFn = js.native
  }
}
