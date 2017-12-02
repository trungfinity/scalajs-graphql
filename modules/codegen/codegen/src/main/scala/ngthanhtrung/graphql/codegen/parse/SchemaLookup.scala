package ngthanhtrung.graphql.codegen.parse

import sangria.ast

// scalastyle:off underscore.import
import sangria.schema._
// scalastyle:on underscore.import

private[parse] final class SchemaLookup(
  schema: Schema[_, _]
) {

  def findType(namedType: ast.NamedType): Result[Type] = {
    schema.allTypes
      .get(namedType.name)
      .toRight(TypeNotFoundException(namedType))
  }

  def findCompositeType(namedType: ast.NamedType): Result[CompositeType[_]] = {
    for {
      tpe <- findType(namedType)
      compositeType <- Typecaster.compositeType(tpe, Some(namedType))
    } yield compositeType: CompositeType[_]
  }

  def findInputType(tpe: ast.Type): Result[InputType[_]] = {
    schema
      .getInputType(tpe)
      .toRight(InputTypeNotFoundException(tpe))
  }

  def findPossibleTypes(
    tpe: CompositeType[_],
    node: ast.AstNode
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
  ): Result[Set[ObjectType[_, _]]] = {
    findPossibleTypes(typeCondition, namedType).map(_.intersect(possibleTypes))
  }
}
