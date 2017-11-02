package anduin.scalajs.apollo

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object link {

  object raw {

    @JSImport("apollo-link", "ApolloLink", "ApolloLink.ApolloLink")
    @js.native
    class ApolloLink extends js.Object

    final class ApolloHttpLinkOptions(
      val uri: String,
      val fetch: js.Any
    ) extends js.Object

    @JSImport("apollo-link-http", "HttpLink", "ApolloLinkHttp.HttpLink")
    @js.native
    class ApolloHttpLink(options: ApolloHttpLinkOptions) extends ApolloLink
  }

  trait ApolloLink {
    type Raw <: link.raw.ApolloLink
    val raw: Raw
  }

  final case class ApolloHttpLinkOptions(
    uri: String,
    fetch: js.Any
  )

  final class ApolloHttpLink(options: ApolloHttpLinkOptions) extends ApolloLink {

    def this(
      uri: String,
      fetch: js.Any
    ) = {
      this(ApolloHttpLinkOptions(
        uri,
        fetch
      ))
    }

    type Raw = link.raw.ApolloHttpLink

    val raw = new link.raw.ApolloHttpLink(
      new link.raw.ApolloHttpLinkOptions(
        options.uri,
        options.fetch
      )
    )
  }
}
