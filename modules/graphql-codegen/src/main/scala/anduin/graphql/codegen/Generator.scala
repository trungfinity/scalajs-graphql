// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

import sangria.{ast, schema => sc}

// scalastyle:off underscore.import
import scala.meta._
// scalastyle:on underscore.import

// scalastyle:off multiple.string.literals

final class Generator(sourceFile: Option[File]) {

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

  private[this] def generateSubfieldTypes(fields: List[tree.Field]): List[Stat] = {
    fields.flatMap {
      case compositeField: tree.CompositeField =>
        generateCompositeField(compositeField)

      case _: tree.SimpleField =>
        List.empty
    }
  }

  private[this] def generateCompositeField(
    compositeField: tree.CompositeField,
    customFieldName: Option[String] = None
  ): List[Stat] = {
    val tpe = compositeField.tpe
    val baseFields = compositeField.fields.getOrElse(tpe, Vector.empty).toList
    val specificFields = compositeField.fields.filterKeys(_ != tpe)

    val fieldName = customFieldName.getOrElse(compositeField.name)
    val className = fieldName.capitalize
    val typeName = Type.Name(className)

    val clazz = if (specificFields.isEmpty) {
      q"""
        final case class $typeName(
          ..${generateSubfieldParams(baseFields, className)}
        )
      """
    } else {
      q"""
        sealed abstract class $typeName extends Product with Serializable {
          ..${generateSubfieldMethods(baseFields, className)}
        }
      """
    }

    List(
      clazz,
      q"""
        object ${Term.Name(className)} {
          ..${generateSubfieldTypes(baseFields)}
        }
      """
    )
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
    } yield {
      val termName = Term.Name(s"${operation.name}$suffix")

      q"""
        object $termName {
          ..${generateCompositeField(operation.underlying, Some("data"))}
        }
      """
    }
  }
}
