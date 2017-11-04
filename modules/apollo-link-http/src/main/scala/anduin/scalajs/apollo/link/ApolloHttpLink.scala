package anduin.scalajs.apollo.link

import anduin.scalajs.fetch.Fetch

final class ApolloHttpLink(options: ApolloHttpLinkOptions) extends ApolloLink {

  def this(
    uri: String,
    fetch: Fetch
  ) = {
    this(
      ApolloHttpLinkOptions(
        uri,
        fetch
      )
    )
  }

  type Raw = internal.ApolloHttpLink

  val raw = new internal.ApolloHttpLink(
    new internal.ApolloHttpLinkOptions(
      options.uri,
      options.fetch.raw.fetch
    )
  )
}
