package ngthanhtrung.graphql.codegen.parse

import scala.language.higherKinds

import cats.MonadError
import sangria.ast
import sangria.validation.TypeInfo

// scalastyle:off underscore.import
import cats.implicits._
import sangria.schema._
// scalastyle:on underscore.import

private[parse] final class SchemaTraversal(
  schema: Schema[_, _]
) {

  private[this] val typeInfo = new TypeInfo(schema)

  private[parse] final class ScopePartiallyApplied[F[_]](val node: ast.AstNode) {

    def apply[A](action: => F[A])(
      implicit error: MonadError[F, ParseException]
    ): F[A] = {
      for {
        _ <- error.pure(typeInfo.enter(node))
        attempt <- error.attempt(action)
        _ = typeInfo.leave(node)
        result <- error.fromEither(attempt)
      } yield result
    }
  }

  def scope[F[_]](node: ast.AstNode): ScopePartiallyApplied[F] = {
    new ScopePartiallyApplied(node)
  }

  def currentNode: Result[ast.AstNode] = {
    typeInfo.ancestors.lastOption.toRight(EmptyNodeStackException)
  }

  def currentType: Result[Type] = {
    for {
      node <- currentNode
      tpe <- typeInfo.tpe.toRight(TypeNotAvailableException(node))
    } yield tpe
  }

  def currentNamedType: Result[Type with Named] = {
    for {
      node <- currentNode
      tpe <- currentType
      namedType <- Typecaster.namedType(tpe, Some(node))
    } yield namedType
  }

  private[this] def specificType[A](
    getNamedType: Boolean
  )(
    filter: (Type, Option[ast.AstNode]) => Result[A]
  ): Result[A] = {
    for {
      node <- currentNode
      tpe <- if (getNamedType) currentNamedType else currentType
      specificType <- filter(tpe, Some(node))
    } yield specificType
  }

  def currentOutputType: Result[OutputType[_]] = {
    specificType(getNamedType = false)(Typecaster.outputType)
  }

  def currentOutputNamedType: Result[OutputType[_]] = {
    specificType(getNamedType = true)(Typecaster.outputType)
  }

  def currentObjectType: Result[ObjectType[_, _]] = {
    specificType(getNamedType = false)(Typecaster.objectType)
  }
}
