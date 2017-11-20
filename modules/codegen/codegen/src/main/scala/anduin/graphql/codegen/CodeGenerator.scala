// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import sangria.{ast, schema}
import sangria.introspection.TypeNameMetaField
import sangria.schema.ObjectType

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
    ast.Field(
      alias = None,
      name = TypeNameMetaField.name,
      arguments = Vector.empty,
      directives = Vector.empty,
      selections = Vector.empty
    ),
    schema.StringType
  )

  private def generateFieldType(tpe: schema.Type)(innermostType: => Type): Type = {
    tpe match {
      case schema.OptionType(innerTpe) =>
        t"$OptionTypeName[${generateFieldType(innerTpe)(innermostType)}]"

      case schema.OptionInputType(innerTpe) =>
        t"$OptionTypeName[${generateFieldType(innerTpe)(innermostType)}]"

      case schema.ListType(innerTpe) =>
        t"$ListTypeName[${generateFieldType(innerTpe)(innermostType)}]"

      case schema.ListInputType(innerTpe) =>
        t"$ListTypeName[${generateFieldType(innerTpe)(innermostType)}]"

      case schema.IDType =>
        Type.Name("ID")

      case _ =>
        innermostType
    }
  }

  private[this] def generateFieldType(field: tree.Field, parentClassName: String): Type = {
    generateFieldType(field.tpe)(
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
    val className = field.name.capitalize

    val baseTypeFields = if (field.subtypeFields.nonEmpty) {
      field.baseTypeFields.toList :+ TypenameField
    } else {
      field.baseTypeFields.toList
    }

    val subtypeFields = field.subtypeFields.toList
    val projectionMethods = subtypeFields.map {
      case (typeCondition, subfields) =>
        val projectionClassName = typeCondition.name.capitalize
        val projectionFullName = s"$className.$projectionClassName"
        val projectionTermName = Term.Name(projectionFullName)
        val projectionReturningType = t"$OptionTypeName[${Type.Name(projectionFullName)}]"

        val subfieldParams = subfields.toList.map { field =>
          Term.Name(field.name)
        }

        q"""
          def ${Term.Name(s"as$projectionClassName")}: $projectionReturningType = {
            if ($projectionTermName.PossibleTypes.contains(__typename)) {
              _root_.scala.Some($projectionTermName(..$subfieldParams))
            } else {
              _root_.scala.None
            }
          }
        """
    }

    val clazz = q"""
      final case class ${Type.Name(className)} (
        ..${generateSubfieldParams(baseTypeFields, className)}
      ) {
        ..$projectionMethods
      }
    """

    val possibleTypeLiterals = field.possibleTypes.toList.map { possibleType =>
      Lit.String(possibleType.name)
    }

    val projectionTypes = subtypeFields.flatMap {
      case (typeCondition, subfields) =>
        // Two hacks are used here:
        // 1. A dummy node is created.
        val node = ast.Field(
          alias = None,
          // Field names are usually in lowercase, however here we use
          // type condition class name directly, which is capitalized.
          name = typeCondition.name,
          arguments = Vector.empty,
          directives = Vector.empty,
          selections = Vector.empty
        )

        // 2. Possible types here are decided based on an assumption that
        //    all subtypes must be ObjectType[_, _]. That assumption is true
        //    but the implementation may change in the future. In particular,
        //    code generator should not know about parsing implementation details.
        generateCompositeField(
          tree.CompositeField(
            node,
            Map(typeCondition -> subfields),
            typeCondition,
            Set(typeCondition.asInstanceOf[ObjectType[_, _]]) // scalastyle:ignore token
          )
        )
    }

    val companionObject = q"""
      object ${Term.Name(className)} {
        val PossibleTypes: $SetTypeName[String] = $SetTermName(..$possibleTypeLiterals)
        ..${generateSubfieldTypes(baseTypeFields)}
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

    packageName.foldLeft[Stat](
      q"""
        object ${Term.Name(s"${operation.name.capitalize}$classNameSuffix")} {
          ..${generateCompositeField(operation.underlyingField)}
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
