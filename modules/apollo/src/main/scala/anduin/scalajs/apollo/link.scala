package anduin.scalajs.apollo

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object link {

  private[apollo] object raw {

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
    private[apollo] type Raw <: link.raw.ApolloLink
    private[apollo] val raw: Raw
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

    private[apollo] type Raw = link.raw.ApolloHttpLink

    private[apollo] val raw = new link.raw.ApolloHttpLink(
      new link.raw.ApolloHttpLinkOptions(
        options.uri,
        options.fetch
      )
    )
  }
}
