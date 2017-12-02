package ngthanhtrung.scalajs.apollo.link.internal

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("apollo-link-http", "HttpLink", "ApolloLinkHttp.HttpLink")
@js.native
class ApolloHttpLink(options: ApolloHttpLinkOptions) extends ApolloLink
