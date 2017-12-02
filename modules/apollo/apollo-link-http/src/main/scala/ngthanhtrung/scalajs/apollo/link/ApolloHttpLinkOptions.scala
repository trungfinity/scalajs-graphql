package ngthanhtrung.scalajs.apollo.link

import ngthanhtrung.scalajs.fetch.Fetch

final case class ApolloHttpLinkOptions(
  uri: String,
  fetch: Fetch
)
