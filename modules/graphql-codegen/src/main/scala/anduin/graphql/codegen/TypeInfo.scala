// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

import scala.util.Try

import sangria.ast

// scalastyle:off underscore.import
import cats.implicits._
import sangria.schema._
// scalastyle:on underscore.import

private[codegen] final class TypeInfo(
  schema: Schema[_, _],
  sourceFile: Option[File]
) {

  private[this] val typeInfo = new sangria.validation.TypeInfo(schema)

  def scope[A](node: ast.AstNode)(action: => Result[A]): Result[A] = {
    for {
      _ <- Right(typeInfo.enter(node))
      result <- action.onError {
        case _ => Right(typeInfo.leave(node))
      }
      _ = typeInfo.leave(node)
    } yield result
  }

  def currentNode: Result[ast.AstNode] = {
    typeInfo.ancestors.lastOption.toRight(EmptyNodeStackException(sourceFile))
  }

  def currentType: Result[Type] = {
    for {
      node <- currentNode
      tpe <- typeInfo.tpe.toRight(TypeNotAvailableException(node, sourceFile))
    } yield tpe
  }

  def currentNamedType: Result[Type with Named] = {
    for {
      node <- currentNode
      tpe <- currentType
      namedType <- Try(tpe.namedType).toEither.left.map {
        NamedTypeNotAvailableException(node, tpe, _, sourceFile)
      }
    } yield namedType
  }

  private[this] def specificCurrentType[A](filter: (ast.AstNode, Type) => Result[A]): Result[A] = {
    for {
      node <- currentNode
      tpe <- currentType
      specificType <- filter(node, tpe)
    } yield specificType
  }

  def currentCompositeType: Result[CompositeType[_]] = {
    specificCurrentType { (node, tpe) =>
      tpe match {
        case compositeType: CompositeType[_] => Right(compositeType)
        case _ => Left(UnexpectedTypeException(node, tpe, classOf[CompositeType[_]], sourceFile))
      }
    }
  }

  def currentObjectType: Result[ObjectType[_, _]] = {
    specificCurrentType { (node, tpe) =>
      tpe match {
        case objectType: ObjectType[_, _] => Right(objectType)
        case _ => Left(UnexpectedTypeException(node, tpe, classOf[ObjectType[_, _]], sourceFile))
      }
    }
  }
}
