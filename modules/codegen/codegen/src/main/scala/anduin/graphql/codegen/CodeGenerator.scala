// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import sangria.{ast, schema}
import sangria.introspection.TypeNameMetaField

import anduin.graphql.codegen.tree.Subfields

// scalastyle:off underscore.import
import scala.meta._
// scalastyle:on underscore.import

// scalastyle:off multiple.string.literals

private[codegen] object CodeGenerator {

  private val ListTypeName = Type.Name("_root_.scala.List")
  private val OptionTypeName = Type.Name("_root_.scala.Option")

  private val SetTermName = Term.Name("_root_.scala.collection.immutable.Set")
  private val SetTypeName = Type.Name("_root_.scala.collection.immutable.Set")

  private val TypenameField = tree.SingleField(
    TypeNameMetaField.name,
    schema.StringType
  )

  private def generateType(tpe: schema.Type)(innermostType: => Type): Type = {
    tpe match {
      case schema.OptionType(innerTpe) =>
        t"$OptionTypeName[${generateType(innerTpe)(innermostType)}]"

      case schema.OptionInputType(innerTpe) =>
        t"$OptionTypeName[${generateType(innerTpe)(innermostType)}]"

      case schema.ListType(innerTpe) =>
        t"$ListTypeName[${generateType(innerTpe)(innermostType)}]"

      case schema.ListInputType(innerTpe) =>
        t"$ListTypeName[${generateType(innerTpe)(innermostType)}]"

      case schema.IDType =>
        Type.Name("ID")

      case _ =>
        innermostType
    }
  }

  private[this] def generateVariableType(variable: tree.Variable): Type = {
    generateType(variable.tpe)(
      Type.Name(variable.tpe.namedType.name)
    )
  }

  private[this] def generateVariableParams(variables: List[tree.Variable]): List[Term.Param] = {
    variables.map { variable =>
      param"${Term.Name(variable.name)}: ${generateVariableType(variable)}"
    }
  }

  private[this] def generateVariables(variables: List[tree.Variable]): List[Stat] = {
    if (variables.nonEmpty) {
      List(
        q"""
          final case class Variables(
            ..${generateVariableParams(variables)}
          )
        """,
        q"""
          object Variables {
            implicit val encoder: _root_.anduin.scalajs.noton.Encoder[Variables] =
              _root_.anduin.scalajs.noton.generic.deriveEncoder[Variables]
          }
        """
      )
    } else {
      List.empty
    }
  }

  private[this] def generateFieldType(field: tree.Field, parentClassName: String): Type = {
    generateType(field.tpe)(
      field match {
        case _: tree.CompositeField =>
          Type.Name(s"$parentClassName.${field.name.capitalize}")

        case _: tree.SingleField =>
          Type.Name(field.tpe.namedType.name)
      }
    )
  }

  private[this] def generateSubfieldParams(
    fields: List[tree.Field],
    parentClassName: String
  ): List[Term.Param] = {
    fields.map { field =>
      param"${Term.Name(field.name)}: ${generateFieldType(field, parentClassName)}"
    }
  }

  private[this] def generateSubfieldTypes(fields: List[tree.Field]): List[Stat] = {
    fields.flatMap {
      case compositeField: tree.CompositeField =>
        generateCompositeField(compositeField)

      case _: tree.SingleField =>
        List.empty
    }
  }

  private[this] def generateCompositeField( // scalastyle:ignore method.length
    field: tree.CompositeField
  ): List[Stat] = {
    val subfields = field.subfields

    val className = field.name.capitalize
    val classTypeName = Type.Name(className)

    val baseFields = if (subfields.projections.nonEmpty) {
      subfields.base.toList :+ TypenameField
    } else {
      subfields.base.toList
    }

    val projectionFields = subfields.projections.toList
    val projectionMethods = projectionFields.map {
      case (typeCondition, fields) =>
        val className = typeCondition.name.capitalize
        val fullName = s"$className.$className"
        val termName = Term.Name(fullName)
        val returningType = t"$OptionTypeName[${Type.Name(fullName)}]"

        val params = fields.toList.map { field =>
          Term.Name(field.name)
        }

        q"""
          def ${Term.Name(s"as$className")}: $returningType = {
            val typename = any.asInstanceOf[_root_.scala.scalajs.js.Dynamic].__typename

            if ($termName.possibleTypes.contains(typename)) {
              _root_.scala.Some($termName(..$params))
            } else {
              _root_.scala.None
            }
          }
        """
    }

    val clazz = q"""
      final case class $classTypeName (
        ..${generateSubfieldParams(baseFields, className)}
      )(any: _root_.scala.scalajs.js.Any) {
        ..$projectionMethods
      }
    """

    val possibleTypeLiterals = field.possibleTypes.toList.map { possibleType =>
      Lit.String(possibleType.name)
    }

    val projectionTypes = projectionFields.flatMap {
      case (typeCondition, fields) =>
        generateCompositeField(
          tree.CompositeField(
            typeCondition.name,
            typeCondition,
            Subfields(fields, Map.empty),
            Vector(typeCondition)
          )
        )
    }

    // There is a known issue: class name, sub-field type names
    // and projection type names could clash.
    val companionObject = q"""
      object ${Term.Name(className)} {
        val possibleTypes: $SetTypeName[String] = $SetTermName(..$possibleTypeLiterals)

        implicit val decoder: _root_.anduin.scalajs.noton.Decoder[$classTypeName] =
          _root_.anduin.scalajs.noton.generic.deriveDecoder[$classTypeName]

        ..${generateSubfieldTypes(baseFields)}
        ..$projectionTypes
      }
    """

    List(clazz, companionObject)
  }

  def generate(operation: tree.Operation, packageName: Option[String] = None): Stat = {
    val classNameSuffix = operation.operationType match {
      case ast.OperationType.Query => "Query"
      case ast.OperationType.Mutation => "Mutation"
      case ast.OperationType.Subscription => "Subscription"
    }

    val rootField = operation.underlyingField.copy(name = "data")

    packageName.foldLeft[Stat](
      q"""
        object ${Term.Name(s"${operation.name.capitalize}$classNameSuffix")} {
          ..${generateVariables(operation.variables.toList)}
          ..${generateCompositeField(rootField)}
        }
      """
    ) { (`object`, packageName) =>
      q"""
        package ${Term.Name(packageName)} {
          ${`object`}
        }
      """
    }
  }
}
