// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.apollo.link

import anduin.scalajs.fetch.Fetch

final case class ApolloHttpLinkOptions(
  uri: String,
  fetch: Fetch
)
