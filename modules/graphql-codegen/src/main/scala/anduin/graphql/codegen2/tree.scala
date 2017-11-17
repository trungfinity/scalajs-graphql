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
    def name: String = node.name
    def node: ast.Field
    def tpe: Type
  }

  final case class SingleField(
    node: ast.Field,
    tpe: Type
  ) extends Field

  final case class CompositeField(
    node: ast.Field,
    subfields: Fields,
    tpe: CompositeType[_],
    possibleTypes: Set[ObjectType[_, _]]
  ) extends Field
}
