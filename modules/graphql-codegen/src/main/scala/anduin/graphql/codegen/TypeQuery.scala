// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

import sangria.ast
import sangria.schema.{AbstractType, CompositeType, ObjectType, Schema, Type}

private[codegen] final class TypeQuery(
  schema: Schema[_, _],
  document: ast.Document,
  sourceFile: Option[File]
) {

  def findFragment(fragmentSpread: ast.FragmentSpread): Result[ast.FragmentDefinition] = {
    document.fragments
      .get(fragmentSpread.name)
      .toRight(FragmentNotFoundException(fragmentSpread, fragmentSpread.name, sourceFile))
  }

  def findType(node: ast.AstNode, name: String): Result[Type] = {
    schema.allTypes
      .get(name)
      .toRight(TypeNotFoundException(node, name, sourceFile))
  }

  def findCompositeType(node: ast.AstNode, name: String): Result[CompositeType[_]] = {
    for {
      tpe <- findType(node, name)
      compositeType <- tpe match {
        case compositeType: CompositeType[_] =>
          Right(compositeType)

        case _ =>
          val compositeTypeClass = classOf[CompositeType[_]]
          Left(ExpectedTypeNotFoundException(node, name, tpe, compositeTypeClass, sourceFile))
      }
    } yield compositeType: CompositeType[_]
  }

  def findPossibleTypes(compositeType: CompositeType[_]): Result[Set[ObjectType[_, _]]] = {
    compositeType match {
      case abstractType: AbstractType =>
        schema.possibleTypes
          .get(compositeType.name)
          .map(_.toSet)
          .toRight(PossibleTypesUnavailableException(abstractType, sourceFile))

      case objectType: ObjectType[_, _] =>
        Right(Set(objectType))
    }
  }

  def narrowPossibleTypes(
    possibleTypes: Set[ObjectType[_, _]],
    conditionType: CompositeType[_]
  ): Result[Set[ObjectType[_, _]]] = {
    findPossibleTypes(conditionType).map(_.intersect(possibleTypes))
  }
}
