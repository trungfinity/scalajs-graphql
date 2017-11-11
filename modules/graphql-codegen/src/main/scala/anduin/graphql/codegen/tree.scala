// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import sangria.{ast, schema}

private[codegen] object tree {

  type Fields = Map[schema.CompositeType[_], Vector[Field]]

  sealed abstract class Tree extends Product with Serializable

  final case class Operation(
    name: String,
    operationType: ast.OperationType,
    underlying: CompositeField
  ) extends Tree

  sealed abstract class Field extends Tree {
    def name: String
  }

  final case class CompositeField(
    name: String,
    fields: Fields,
    tpe: schema.CompositeType[_]
  ) extends Field

  final case class SimpleField(
    name: String,
    tpe: schema.Type
  ) extends Field
}
