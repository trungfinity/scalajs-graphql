// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

import sangria.{ast, schema => sc}

// scalastyle:off underscore.import
import scala.meta._

import cats.implicits._
// scalastyle:on underscore.import

// scalastyle:off multiple.string.literals

final class Generator(
  transformer: Transformer,
  sourceFile: Option[File]
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

    val baseFields = fields.getOrElse(tpe, Vector.empty).toList

    for {
      subfieldTypes <- generateSubfieldTypes(baseFields)

      specificFields <- fields
        .filterKeys(_ != tpe)
        .toList
        .foldMapM[Result, tree.Fields] {
          case (typeCondition, subfields) =>
            // Make sure projection classes have all fields from the base class
            transformer.mergeFields(subfields ++ baseFields.toVector).map { subfields =>
              Map(typeCondition -> subfields)
            }
        }

      specificSubfieldTypes <- specificFields.toList.foldMapM[Result, List[Stat]] {
        case (typeCondition, subfields) =>
          generateCompositeField(
            tree.CompositeField(typeCondition.name, Map(typeCondition -> subfields), typeCondition)
          )
      }
    } yield {
      val fieldName = customFieldName.getOrElse(compositeField.name)
      val className = fieldName.capitalize
      val classStats = specificFields.toList.map {
        case (typeCondition, _) =>
          val termName = Term.Name(s"as${typeCondition.name}")
          val typeName = Type.Name(s"$className.${typeCondition.name}")
          q"def $termName: Option[$typeName] = ???"
      }

      val clazz = q"""
        final case class ${Type.Name(className)} (
          ..${generateSubfieldParams(baseFields, className)}
        ) {
          ..$classStats
        }
      """

      val companion = Some(subfieldTypes ++ specificSubfieldTypes)
        .filter(_.nonEmpty)
        .map { stats =>
          q"""
            object ${Term.Name(className)} {
              ..$stats
            }
          """
        }

      List(clazz) ++ companion.toList
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
