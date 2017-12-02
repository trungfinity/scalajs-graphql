package ngthanhtrung.graphql.codegen

package object cli {
  private[cli] type Result[A] = Either[Throwable, A]
}
