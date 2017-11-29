// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen.parse

import scala.language.higherKinds
import scala.util.Try

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
      namedType <- Try(tpe.namedType).toEither.left.map {
        NamedTypeNotAvailableException(tpe, node, _)
      }
    } yield namedType
  }

  private[this] def specificType[A](
    getType: => Result[Type]
  )(
    filter: (ast.AstNode, Type) => Result[A]
  ): Result[A] = {
    for {
      node <- currentNode
      tpe <- getType
      specificType <- filter(node, tpe)
    } yield specificType
  }

  private[this] def filterOutputType(node: ast.AstNode, tpe: Type): Result[OutputType[_]] = {
    tpe match {
      case outputType: OutputType[_] => Right(outputType)
      case _ => Left(UnexpectedTypeException(tpe, classOf[OutputType[_]], node))
    }
  }

  def currentOutputType: Result[OutputType[_]] = {
    specificType(currentType)(filterOutputType)
  }

  def currentOutputNamedType: Result[OutputType[_]] = {
    specificType(currentNamedType)(filterOutputType)
  }

  def currentObjectType: Result[ObjectType[_, _]] = {
    specificType(currentType) { (node, tpe) =>
      tpe match {
        case objectType: ObjectType[_, _] => Right(objectType)
        case _ => Left(UnexpectedTypeException(tpe, classOf[ObjectType[_, _]], node))
      }
    }
  }
}
