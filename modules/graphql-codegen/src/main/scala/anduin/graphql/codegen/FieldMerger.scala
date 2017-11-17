// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import cats.data.NonEmptyVector

// scalastyle:off underscore.import
import cats.implicits._
// scalastyle:on underscore.import

private[codegen] object FieldMerger {

  private[this] def mergeSameNameFields(
    fields: NonEmptyVector[tree.Field]
  )(
    implicit sourceFile: Option[SourceFile]
  ): Result[tree.Field] = {
    fields.head match {
      case singleField: tree.SingleField =>
        fields.tail.foldLeftM[Result, tree.SingleField] {
          singleField
        } { (mergedField, field) =>
          if (field.tpe == mergedField.tpe) {
            Right(mergedField)
          } else {
            Left(ConflictedFieldsException(mergedField, field))
          }
        }

      case compositeField: tree.CompositeField =>
        // We use `fields` here instead of `fields.tail`
        // because if there is only one field, we want to merge its subfields as well.
        fields.foldLeftM[Result, tree.CompositeField] {
          compositeField
        } { (mergedField, field) =>
          field match {
            case compositeField: tree.CompositeField if compositeField.tpe == mergedField.tpe =>
              val mergedSubfields = mergedField.subfields.combine(compositeField.subfields)
              merge(mergedField.copy(subfields = mergedSubfields))

            case _ =>
              Left(ConflictedFieldsException(mergedField, field))
          }
        }
    }
  }

  private[this] def mergeFields(
    fields: Vector[tree.Field]
  )(
    implicit sourceFile: Option[SourceFile]
  ): Result[Vector[tree.Field]] = {
    fields
      .groupBy(_.name)
      .values
      .toVector
      .foldMapM[Result, Vector[tree.Field]] { sameNameFields =>
        sameNameFields.toNev
          .map { mergeSameNameFields(_).map(Vector(_)) }
          .getOrElse(Right(Vector.empty))
      }
      .map(_.sortBy(_.name))
  }

  def merge(field: tree.CompositeField)(
    implicit sourceFile: Option[SourceFile]
  ): Result[tree.CompositeField] = {
    // Three equirements:
    // 1. Duplicate sub-fields must be deeply merged
    // 2. Sub-types must have all fields from the base type
    // 3. Fields must be listed in alphabetical order

    for {
      baseTypeFields <- mergeFields(field.baseTypeFields)
      subtypeFields <- field.subtypeFields.toVector.foldMapM[Result, tree.Fields] {
        case (subtype, fields) =>
          mergeFields(fields).map(fields => Map(subtype -> fields))
      }
    } yield {
      val subfields = Map(field.tpe -> baseTypeFields) ++ subtypeFields
      field.copy(subfields = subfields)
    }
  }
}
