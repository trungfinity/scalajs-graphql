package ngthanhtrung.scalajs.apollo.graphqltools.internal

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("graphql-tools", JSImport.Namespace, "GraphqlTools")
@js.native
object GraphqlTools extends js.Object {
  def makeExecutableSchema(options: ExecutableSchemaOptions): js.Any = js.native
  def addMockFunctionsToSchema(options: SchemaMockFunctionsOptions): Unit = js.native
}
