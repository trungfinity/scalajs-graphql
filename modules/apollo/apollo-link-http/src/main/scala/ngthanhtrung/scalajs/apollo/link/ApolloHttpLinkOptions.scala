// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.scalajs.apollo.link

import ngthanhtrung.scalajs.fetch.Fetch

final case class ApolloHttpLinkOptions(
  uri: String,
  fetch: Fetch
)
