// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

import sangria.{ast, schema => sc}

private[codegen] final class SchemaLookup(
  schema: sc.Schema[_, _],
  sourceFile: Option[File]
) {

  def findType(node: ast.AstNode, name: String): Result[sc.Type] = {
    schema.allTypes
      .get(name)
      .toRight(TypeNotFoundException(node, name, sourceFile))
  }

  def findCompositeType(node: ast.AstNode, name: String): Result[sc.CompositeType[_]] = {
    for {
      tpe <- findType(node, name)
      compositeType <- tpe match {
        case compositeType: sc.CompositeType[_] =>
          Right(compositeType)

        case _ =>
          val compositeTypeClass = classOf[sc.CompositeType[_]]
          Left(ExpectedTypeNotFoundException(node, name, tpe, compositeTypeClass, sourceFile))
      }
    } yield compositeType: sc.CompositeType[_]
  }

  def findPossibleTypes(compositeType: sc.CompositeType[_]): Result[Set[sc.ObjectType[_, _]]] = {
    compositeType match {
      case abstractType: sc.AbstractType =>
        schema.possibleTypes
          .get(compositeType.name)
          .map(_.toSet)
          .toRight(PossibleTypesUnavailableException(abstractType, sourceFile))

      case objectType: sc.ObjectType[_, _] =>
        Right(Set(objectType))
    }
  }

  def narrowPossibleTypes(
    possibleTypes: Set[sc.ObjectType[_, _]],
    conditionType: sc.CompositeType[_]
  ): Result[Set[sc.ObjectType[_, _]]] = {
    findPossibleTypes(conditionType).map(_.intersect(possibleTypes))
  }
}
