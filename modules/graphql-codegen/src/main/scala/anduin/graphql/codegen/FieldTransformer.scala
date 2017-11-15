// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

// scalastyle:off underscore.import
import cats.implicits._
// scalastyle:on underscore.import

private[codegen] final class FieldTransformer(
  sourceFile: Option[File],
  schemaLookup: SchemaLookup
) {

  private[this] def mergeSameNameFields(fields: Vector[tree.Field]): Result[Vector[tree.Field]] = {
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
              case currentField: tree.CompositeField if currentField.tpe == mergedField.tpe =>
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

  def mergeFields(fields: Vector[tree.Field]): Result[Vector[tree.Field]] = {
    fields
      .groupBy(_.name)
      .values
      .toVector
      .foldMapM(mergeSameNameFields)
  }

  def transformField(field: tree.CompositeField): Result[tree.CompositeField] = {
    for {
      possibleTypes <- schemaLookup.findPossibleTypes(field.tpe)

      // Flatten type conditions. Basically, there are 2 cases:
      // 1. If a type condition is super set of the current composite type,
      //    lift all subfields to the context of the current type.
      // 2. If a type condition overlaps the current type, bring subfields
      //    to each overlapping possible types independently.
      transformedSubfields <- field.fields.toVector.foldMapM[Result, tree.Fields] {
        case (conditionType, subfields) =>
          for {
            narrowedPossibleTypes <- schemaLookup.narrowPossibleTypes(possibleTypes, conditionType)
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
}
