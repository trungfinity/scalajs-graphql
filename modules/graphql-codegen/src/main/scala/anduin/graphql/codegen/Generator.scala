// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

import sangria.{ast, schema => sc}
import sangria.introspection.TypeNameMetaField

// scalastyle:off underscore.import
import scala.meta._

import cats.implicits._
// scalastyle:on underscore.import

// scalastyle:off multiple.string.literals

final class Generator(
  sourceFile: Option[File],
  schemaLookup: SchemaLookup,
  documentTransformer: DocumentTransformer,
  fieldTransformer: FieldTransformer
) {

  private[this] def generateFieldType(field: tree.Field, parentClassName: String): Type = {
    val innermostType = field match {
      case _: tree.CompositeField =>
        Type.Name(s"$parentClassName.${field.name.capitalize}")

      case _: tree.SimpleField =>
        Type.Name(field.tpe.namedType.name)
    }

    def typeOf(tpe: sc.Type): Type = {
      tpe match {
        case sc.OptionType(innerTpe) => t"Option[${typeOf(innerTpe)}]"
        case sc.OptionInputType(innerTpe) => t"Option[${typeOf(innerTpe)}]"

        case sc.ListType(innerTpe) => t"List[${typeOf(innerTpe)}]"
        case sc.ListInputType(innerTpe) => t"List[${typeOf(innerTpe)}]"

        case sc.IDType => Type.Name("ID")

        case _ => innermostType
      }
    }

    typeOf(field.tpe)
  }

  private[this] def generateSubfieldParams(
    fields: List[tree.Field],
    parentFieldName: String
  ): List[Term.Param] = {
    fields.map { field =>
      val termName = Term.Name(field.name)
      val tpe = generateFieldType(field, parentFieldName)
      param"$termName: $tpe"
    }
  }

  private[this] def generateSubfieldMethods(
    fields: List[tree.Field],
    parentFieldName: String
  ): List[Decl.Def] = {
    fields.map { field =>
      val termName = Term.Name(field.name)
      val tpe = generateFieldType(field, parentFieldName)
      q"def $termName: $tpe"
    }
  }

  private[this] def generateSubfieldTypes(fields: List[tree.Field]): Result[List[Stat]] = {
    fields.foldMapM[Result, List[Stat]] {
      case compositeField: tree.CompositeField =>
        generateCompositeField(compositeField)

      case _: tree.SimpleField =>
        Right(List.empty)
    }
  }

  def printFields(fields: tree.Fields, indentation: Int): Unit = {
    fields.foreach {
      case (container, fieldsByContainer) =>
        fieldsByContainer.foreach { field =>
          print(" " * indentation)
          print(container.name)
          print(": ")

          field match {
            case tree.CompositeField(name, subfields, _) =>
              println(name)
              printFields(subfields, indentation + 2)

            case tree.SimpleField(name, tpe) =>
              print(name)
              print(" -> ")
              println(tpe.namedType.name)
          }
        }
    }
  }

  private[this] def generateCompositeField( // scalastyle:ignore method.length
    compositeField: tree.CompositeField,
    customFieldName: Option[String] = None
  ): Result[List[Stat]] = {
    val tpe = compositeField.tpe
    val fields = compositeField.fields

    val baseFields0 = fields.getOrElse(tpe, Vector.empty).toList
    val specificFields0 = fields.filterKeys(_ != tpe)
    val baseFields1 = if (specificFields0.nonEmpty) {
      baseFields0 :+ tree.SimpleField(TypeNameMetaField.name, sc.StringType)
    } else {
      baseFields0
    }

    val baseFields = baseFields1.sortBy(_.name)

    for {
      subfieldTypes <- generateSubfieldTypes(baseFields)

      specificFields <- fields
        .filterKeys(_ != tpe)
        .toList
        .foldMapM[Result, tree.Fields] {
          case (typeCondition, subfields) =>
            // Make sure projection classes have all fields from the base class
            fieldTransformer.mergeFields(subfields ++ baseFields.toVector).map { subfields =>
              Map(typeCondition -> subfields)
            }
        }

      specificSubfieldTypes <- specificFields.toList.foldMapM[Result, List[Stat]] {
        case (typeCondition, subfields) =>
          generateCompositeField(
            tree.CompositeField(typeCondition.name, Map(typeCondition -> subfields), typeCondition)
          )
      }

      possibleTypes <- schemaLookup.findPossibleTypes(compositeField.tpe)
    } yield {
      val fieldName = customFieldName.getOrElse(compositeField.name)
      val className = fieldName.capitalize
      val classStats = specificFields.toList.map {
        case (typeCondition, subfields) =>
          val projectionName = typeCondition.name.capitalize
          val projectionFullName = s"$className.$projectionName"
          val projectionTermName = Term.Name(projectionFullName)

          val subfieldParams = subfields.toList.map { field =>
            Term.Name(field.name)
          }

          q"""
            def ${Term.Name(s"as$projectionName")}: Option[${Type.Name(projectionFullName)}] = {
              if ($projectionTermName.PossibleTypes.contains(__typename)) {
                Some($projectionTermName(..$subfieldParams))
              } else {
                None
              }
            }
          """
      }

      val clazz = q"""
        final case class ${Type.Name(className)} (
          ..${generateSubfieldParams(baseFields, className)}
        ) {
          ..$classStats
        }
      """

      val possibleTypeLiterals = possibleTypes.toList.map { possibleType =>
        Lit.String(possibleType.name)
      }

      val companionStats = subfieldTypes ++ specificSubfieldTypes
      val companion = q"""
        object ${Term.Name(className)} {
          val PossibleTypes: Set[String] = Set(..$possibleTypeLiterals)
          ..$companionStats
        }
      """

      List(clazz, companion)
    }
  }

  def generate(operation: tree.Operation): Result[Defn.Object] = {
    for {
      suffix <- operation.operationType match {
        case ast.OperationType.Query =>
          Right("Query")

        case ast.OperationType.Mutation =>
          Right("Mutation")

        case ast.OperationType.Subscription =>
          Left(
            OperationTypeNotSupportedException(
              operation.operationType,
              operation.name,
              sourceFile
            )
          )
      }

      stats <- generateCompositeField(operation.underlying, Some("data"))
    } yield {
      q"""
        object ${Term.Name(s"${operation.name}$suffix")} {
          ..$stats
        }
      """
    }
  }
}
