// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

import scala.util.Try

import sangria.{ast, schema => sc}

// scalastyle:off underscore.import
import cats.implicits._
// scalastyle:on underscore.import

private[codegen] final class TypeInfo(
  schema: sc.Schema[_, _],
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

  def currentType: Result[sc.Type] = {
    for {
      node <- currentNode
      tpe <- typeInfo.tpe.toRight(TypeNotAvailableException(node, sourceFile))
    } yield tpe
  }

  def currentNamedType: Result[sc.Type with sc.Named] = {
    for {
      node <- currentNode
      tpe <- currentType
      namedType <- Try(tpe.namedType).toEither.left.map {
        NamedTypeNotAvailableException(node, tpe, _, sourceFile)
      }
    } yield namedType
  }

  private[this] def specificCurrentType[A](
    filter: (ast.AstNode, sc.Type) => Result[A]
  ): Result[A] = {
    for {
      node <- currentNode
      tpe <- currentType
      specificType <- filter(node, tpe)
    } yield specificType
  }

  def currentCompositeType: Result[sc.CompositeType[_]] = {
    specificCurrentType { (node, tpe) =>
      tpe match {
        case compositeType: sc.CompositeType[_] => Right(compositeType)
        case _ => Left(UnexpectedTypeException(node, tpe, classOf[sc.CompositeType[_]], sourceFile))
      }
    }
  }

  def currentObjectType: Result[sc.ObjectType[_, _]] = {
    specificCurrentType { (node, tpe) =>
      tpe match {
        case objectType: sc.ObjectType[_, _] => Right(objectType)
        case _ => Left(UnexpectedTypeException(node, tpe, classOf[sc.ObjectType[_, _]], sourceFile))
      }
    }
  }
}
