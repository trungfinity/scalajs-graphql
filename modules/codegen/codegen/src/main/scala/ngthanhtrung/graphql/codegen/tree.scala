// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.graphql.codegen

import cats.kernel.Monoid
import sangria.ast.OperationType
import sangria.schema.{InputType, ObjectType, OutputType}

// scalastyle:off underscore.import
import cats.implicits._
// scalastyle:on underscore.import

object tree {

  sealed trait Tree extends Product with Serializable

  final case class Variable(
    name: String,
    tpe: InputType[_]
  ) extends Tree

  sealed trait Field extends Tree {
    def name: String
    def tpe: OutputType[_]
  }

  final case class SingleField(
    name: String,
    tpe: OutputType[_]
  ) extends Field

  final case class Subfields(
    base: Vector[Field],
    projections: Map[ObjectType[_, _], Vector[Field]]
  )

  object Subfields {

    implicit val subfieldsMonoid: Monoid[Subfields] = new Monoid[Subfields] {

      final def empty: Subfields = {
        Subfields(base = Vector.empty, projections = Map.empty)
      }

      final def combine(x: Subfields, y: Subfields): Subfields = {
        Subfields(
          base = x.base.combine(y.base),
          projections = x.projections.combine(y.projections)
        )
      }
    }
  }

  final case class CompositeField(
    name: String,
    tpe: OutputType[_],
    subfields: Subfields,
    possibleTypes: Vector[ObjectType[_, _]]
  ) extends Field

  final case class Operation(
    name: String,
    operationType: OperationType,
    variables: Vector[Variable],
    underlyingField: CompositeField,
    source: String
  ) extends Tree
}
