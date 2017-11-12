// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

import sangria.ast.Document

// scalastyle:off underscore.import
import cats.implicits._
import sangria.schema._
// scalastyle:on underscore.import

final class Transformer(
  schema: Schema[_, _],
  document: Document,
  sourceFile: Option[File]
) {

  private[this] val typeQuery = new TypeQuery(schema, document, sourceFile)

  private[this] def transformField(field: tree.CompositeField): Result[tree.CompositeField] = {
    for {
      possibleTypes <- typeQuery.findPossibleTypes(field.tpe)

      // Flatten type conditions. Basically, there are 2 cases:
      // 1. If a type condition is super set of the current composite type,
      //    lift all subfields to the context of the current type.
      // 2. If a type condition overlaps the current type, bring subfields
      //    to each overlapping possible types independently.
      transformedSubfields <- field.fields.toVector.foldMapM[Result, tree.Fields] {
        case (conditionType, subfields) =>
          for {
            narrowedPossibleTypes <- typeQuery.narrowPossibleTypes(possibleTypes, conditionType)
          } yield {
            val targetConditionTypes = if (narrowedPossibleTypes.size >= possibleTypes.size) {
              // This is the first case explained above.
              Set(field.tpe)
            } else {
              // This is the second one.
              narrowedPossibleTypes
            }

            targetConditionTypes.map(_ -> subfields).toMap
          }
      }

      // Merge nested subfields
      mergedSubfields <- transformedSubfields.toVector.foldMapM[Result, tree.Fields] {
        case (conditionType, subFields) =>
          mergeFields(subFields).map { mergedSubfields =>
            Map(conditionType -> mergedSubfields)
          }
      }
    } yield {
      field.copy(fields = mergedSubfields)
    }
  }

  private[this] def mergeable(
    firstField: tree.CompositeField,
    secondField: tree.CompositeField
  ): Boolean = {
    firstField.name == secondField.name && firstField.tpe == secondField.tpe
  }

  private[this] def mergeFields(fields: Vector[tree.Field]): Result[Vector[tree.Field]] = {
    fields
      .groupBy(_.name)
      .values
      .toVector
      .foldMapM[Result, Vector[tree.Field]] { fields =>
        fields.headOption match {
          case Some(simpleField: tree.SimpleField) =>
            fields
              .drop(1)
              .foldLeftM[Result, Vector[tree.Field]] {
                Vector(simpleField)
              } { (mergeFields, field) =>
                if (field == simpleField) {
                  Right(mergeFields)
                } else {
                  Left(ConflictedFieldsException(simpleField, field, sourceFile))
                }
              }

          case Some(compositeField: tree.CompositeField) =>
            for {
              // We use `fields` here instead of `fields.drop(1)`
              // because if there is only one field, we want to merge its subfields as well.
              mergedField <- fields.foldLeftM[Result, tree.CompositeField] {
                compositeField
              } { (mergedField, field) =>
                field match {
                  case currentField: tree.CompositeField if mergeable(mergedField, currentField) =>
                    val mergedSubfields = mergedField.fields.combine(currentField.fields)
                    transformField(mergedField.copy(fields = mergedSubfields))

                  case _ =>
                    Left(ConflictedFieldsException(mergedField, field, sourceFile))
                }
              }
            } yield Vector(mergedField)

          case None =>
            Right(Vector.empty)
        }
      }
  }

  def transform(operation: tree.Operation): Result[tree.Operation] = {
    for {
      transformedUnderlying <- transformField(operation.underlying)
    } yield {
      operation.copy(underlying = transformedUnderlying)
    }
  }
}
