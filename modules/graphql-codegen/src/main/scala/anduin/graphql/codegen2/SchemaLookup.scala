// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen2

import sangria.ast

// scalastyle:off underscore.import
import sangria.schema._
// scalastyle:on underscore.import

private[codegen2] final class SchemaLookup(
  schema: Schema[_, _]
) {

  def findType(namedType: ast.NamedType)(
    implicit sourceFile: Option[SourceFile]
  ): Result[Type] = {
    schema.allTypes
      .get(namedType.name)
      .toRight(TypeNotFoundException(namedType))
  }

  def findCompositeType(namedType: ast.NamedType)(
    implicit sourceFile: Option[SourceFile]
  ): Result[CompositeType[_]] = {
    for {
      tpe <- findType(namedType)
      compositeType <- tpe match {
        case compositeType: CompositeType[_] => Right(compositeType)
        case _ => Left(UnexpectedTypeException(tpe, classOf[CompositeType[_]], namedType))
      }
    } yield compositeType: CompositeType[_]
  }

  def findPossibleTypes(
    tpe: CompositeType[_],
    node: ast.AstNode
  )(
    implicit sourceFile: Option[SourceFile]
  ): Result[Set[ObjectType[_, _]]] = {
    tpe match {
      case abstractType: AbstractType =>
        schema.possibleTypes
          .get(tpe.name)
          .map(_.toSet)
          .toRight(PossibleTypesUnavailableException(abstractType, node))

      case objectType: ObjectType[_, _] =>
        Right(Set(objectType))
    }
  }

  def narrowPossibleTypes(
    possibleTypes: Set[ObjectType[_, _]],
    typeCondition: CompositeType[_],
    namedType: ast.NamedType
  )(
    implicit sourceFile: Option[SourceFile]
  ): Result[Set[ObjectType[_, _]]] = {
    findPossibleTypes(typeCondition, namedType).map(_.intersect(possibleTypes))
  }
}
