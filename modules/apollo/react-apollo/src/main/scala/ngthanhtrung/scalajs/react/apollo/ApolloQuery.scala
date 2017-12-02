package ngthanhtrung.scalajs.react.apollo

import scala.scalajs.js

trait ApolloQuery {
  type Variables
  type Data
  val raw: internal.Query[js.Object, js.Object]
}
