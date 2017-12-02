package ngthanhtrung.graphql.codegen.parse

import cats.data.NonEmptyVector
import sangria.schema.ObjectType

import ngthanhtrung.graphql.codegen.tree

// scalastyle:off underscore.import
import cats.implicits._
// scalastyle:on underscore.import

private[parse] object FieldMerger {

  private[this] def mergeSameNameFields(
    fields: NonEmptyVector[tree.Field]
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
              merge(
                mergedField.copy(
                  subfields = mergedField.subfields.combine(compositeField.subfields)
                )
              )

            case _ =>
              Left(ConflictedFieldsException(mergedField, field))
          }
        }
    }
  }

  private[this] def mergeFields(fields: Vector[tree.Field]): Result[Vector[tree.Field]] = {
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

  def merge(field: tree.CompositeField): Result[tree.CompositeField] = {
    // Three requirements:
    // 1. Duplicate sub-fields must be deeply merged
    // 2. Projection fields must contain all base fields
    // 3. Fields must be listed in alphabetical order

    for {
      baseFields <- mergeFields(field.subfields.base)
      projectionFields <- field.subfields.projections.toVector
        .foldMapM[Result, Map[ObjectType[_, _], Vector[tree.Field]]] {
          case (projection, fields) =>
            mergeFields(fields).map(fields => Map(projection -> fields))
        }
    } yield {
      field.copy(
        subfields = field.subfields.copy(
          base = baseFields,
          projections = projectionFields
        )
      )
    }
  }
}
