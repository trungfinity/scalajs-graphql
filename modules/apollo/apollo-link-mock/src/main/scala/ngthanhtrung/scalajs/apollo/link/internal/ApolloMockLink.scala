package ngthanhtrung.scalajs.apollo.link.internal

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport(
  "./ngthanhtrung/scalajs/apollo/link/MockLink.js",
  "MockLink",
  "ApolloLinkMock.MockLink"
)
@js.native
class ApolloMockLink(options: ApolloMockLinkOptions) extends ApolloLink
