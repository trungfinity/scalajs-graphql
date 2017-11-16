// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen2

import sangria.ast
import sangria.schema.{CompositeType, ObjectType, Type}

private[codegen2] object tree {

  type Fields = Map[CompositeType[_], Vector[Field]]

  sealed abstract class Tree extends Product with Serializable

  final case class Operation(
    name: String,
    operationType: ast.OperationType,
    underlyingField: CompositeField
  ) extends Tree

  sealed abstract class Field extends Tree {
    def name: String
    def tpe: Type
  }

  final case class SingleField(
    name: String,
    tpe: Type
  ) extends Field

  final case class CompositeField(
    name: String,
    subfields: Fields,
    tpe: CompositeType[_],
    possibleTypes: Set[ObjectType[_, _]]
  ) extends Field
}
