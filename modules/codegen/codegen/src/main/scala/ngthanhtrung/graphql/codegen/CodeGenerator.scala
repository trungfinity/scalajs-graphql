// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.graphql.codegen

import sangria.{ast, schema}
import sangria.introspection.TypeNameMetaField

import ngthanhtrung.graphql.codegen.tree.Subfields

// scalastyle:off underscore.import
import scala.meta._
// scalastyle:on underscore.import

// scalastyle:off multiple.string.literals

private[codegen] object CodeGenerator {

  private val ListTypeName = Type.Name("_root_.scala.List")
  private val OptionTypeName = Type.Name("_root_.scala.Option")

  private val SetTermName = Term.Name("_root_.scala.collection.immutable.Set")
  private val SetTypeName = Type.Name(SetTermName.value)

  private val EncoderTermName = Term.Name("_root_.ngthanhtrung.scalajs.noton.Encoder")
  private val EncoderTypeName = Type.Name(EncoderTermName.value)
  private val DecoderTermName = Term.Name("_root_.ngthanhtrung.scalajs.noton.Decoder")
  private val DecoderTypeName = Type.Name(DecoderTermName.value)

  private val TypenameField = tree.SingleField(
    TypeNameMetaField.name,
    schema.StringType
  )

  private def generateSource(stats: List[Stat], packageName: Option[String]): Source = {
    packageName.foldLeft[Source](
      Source(stats)
    ) { (source, packageName) =>
      val stat = q"""
        package ${Term.Name(packageName)} {
          ..${source.stats}
        }
      """

      Source(List(stat))
    }
  }

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
        Type.Name("String")

      case _ =>
        innermostType
    }
  }

  private def generateInputType(tpe: schema.Type): Type = {
    generateType(tpe)(Type.Name(tpe.namedType.name))
  }

  private def generateFieldType(field: tree.Field, parentClassName: String): Type = {
    generateType(field.tpe)(
      field match {
        case _: tree.CompositeField =>
          Type.Name(s"$parentClassName.${field.name.capitalize}")

        case _: tree.SingleField =>
          Type.Name(field.tpe.namedType.name)
      }
    )
  }

  def generateEnumType[A](
    tpe: schema.EnumType[A],
    packageName: Option[String] = None
  ): Source = {
    val typeName = Type.Name(tpe.name)

    val valueTermNames = tpe.values.map(value => Term.Name(value.name))
    val valueClasses: List[Stat] = tpe.values.map { value =>
      q"""
        case object ${Term.Name(value.name)} extends {
          val value: String = ${Lit.String(tpe.coerceOutput(value.value))}
        } with ${init"$typeName()"}
      """
    }

    val invalidValueLiteral = Lit.String(s" is not a valid value of ${tpe.name} enum.")

    val stats = valueClasses ++ List(
      q"val values: $SetTypeName[$typeName] = $SetTermName(..$valueTermNames)",
      q"""
        implicit val encoder: $EncoderTypeName[$typeName] = $EncoderTermName(_.value)
      """,
      q"""
        implicit val decoder: $DecoderTypeName[$typeName] = $DecoderTermName[String]
          .emap { maybeValue =>
            values
              .find(_.value == maybeValue)
              .toRight(new RuntimeException(maybeValue + $invalidValueLiteral))
          }
      """
    )

    generateSource(
      List(
        q"""
          sealed abstract class ${Type.Name(tpe.name)} extends Product with Serializable {
            def value: String
          }
        """,
        q"""
          object ${Term.Name(tpe.name)} {
            ..$stats
          }
        """
      ),
      packageName
    )
  }

  def generateInputObjectType(
    tpe: schema.InputObjectType[_],
    packageName: Option[String] = None
  ): Source = {
    val className = tpe.name.capitalize
    val typeName = Type.Name(className)

    val params = tpe.fields.map { field =>
      param"${Term.Name(field.name)}: ${generateInputType(field.fieldType)}"
    }

    generateSource(
      List(
        q"final case class $typeName(..$params)",
        q"""
          object ${Term.Name(className)} {
            implicit val encoder: $EncoderTypeName[$typeName] =
              _root_.ngthanhtrung.scalajs.noton.generic.deriveEncoder[$typeName]
          }
        """
      ),
      packageName
    )
  }

  private def generateVariables(variables: List[tree.Variable]): List[Stat] = {
    if (variables.nonEmpty) {
      val params = variables.map { variable =>
        param"${Term.Name(variable.name)}: ${generateInputType(variable.tpe)}"
      }

      List(
        q"final case class Variables(..$params)",
        q"""
          object Variables {
            implicit val encoder: $EncoderTypeName[Variables] =
              _root_.ngthanhtrung.scalajs.noton.generic.deriveEncoder[Variables]
          }
        """
      )
    } else {
      List(
        q"type Variables = Unit"
      )
    }
  }

  private def generateSubfieldTypes(fields: List[tree.Field]): List[Stat] = {
    fields.flatMap {
      case compositeField: tree.CompositeField =>
        generateCompositeField(compositeField)

      case _: tree.SingleField =>
        List.empty
    }
  }

  private def generateCompositeField( // scalastyle:ignore method.length
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

    val params = baseFields.map { field =>
      param"${Term.Name(field.name)}: ${generateFieldType(field, className)}"
    }

    val possibleTypeLiterals = field.possibleTypes.toList.map { possibleType =>
      Lit.String(possibleType.name)
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
            val typename = raw.asInstanceOf[_root_.scala.scalajs.js.Dynamic].__typename

            if ($termName.possibleTypes.contains(typename)) {
              _root_.scala.Some($termName(..$params))
            } else {
              _root_.scala.None
            }
          }
        """
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

    List(
      q"""
        final case class $classTypeName(..$params)(raw: _root_.scala.scalajs.js.Any) {
          ..$projectionMethods
        }
      """,
      // There is a known issue: class name, sub-field type names
      // and projection type names could clash.
      q"""
        object ${Term.Name(className)} {
          val possibleTypes: $SetTypeName[String] = $SetTermName(..$possibleTypeLiterals)

          implicit val decoder: $DecoderTypeName[$classTypeName] =
            _root_.ngthanhtrung.scalajs.noton.generic.deriveDecoder[$classTypeName]

          ..${generateSubfieldTypes(baseFields)}
          ..$projectionTypes
        }
      """
    )
  }

  def generateOperation(operation: tree.Operation, packageName: Option[String] = None): Source = {
    val classNameSuffix = operation.operationType match {
      case ast.OperationType.Query => "Query"
      case ast.OperationType.Mutation => "Mutation"
      case ast.OperationType.Subscription => "Subscription"
    }

    val rootField = operation.underlyingField.copy(name = "data")

    generateSource(
      List(
        q"""
          object ${Term.Name(s"${operation.name.capitalize}$classNameSuffix")}
            extends _root_.ngthanhtrung.scalajs.react.apollo.ApolloQuery() {

            val raw = _root_.ngthanhtrung.scalajs.react.apollo.internal.GraphqlTag.gql[
              _root_.scala.scalajs.js.Object,
              _root_.scala.scalajs.js.Object
            ](
              ${Lit.String(operation.source)}
            )

            ..${generateVariables(operation.variables.toList)}
            ..${generateCompositeField(rootField)}
          }
        """
      ),
      packageName
    )
  }
}
