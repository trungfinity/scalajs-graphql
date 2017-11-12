// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

import sangria.{ast, schema => sc}

// scalastyle:off underscore.import
import scala.meta._
// scalastyle:on underscore.import

final class Generator(
  schema: sc.Schema[_, _],
  document: ast.Document,
  sourceFile: Option[File]
) {

  private[this] val typeQuery = new TypeQuery(schema, document, sourceFile)

  private[this] def termParam(name: String, tpe: Type): Term.Param = {
    Term.Param(List.empty, Term.Name(name), Some(tpe), None)
  }

  def generateFieldType(field: tree.Field)(genType: sc.Type => Type): Type = {
    def typeOf(tpe: sc.Type): Type = tpe match {
      case sc.OptionType(wrapped) =>
        t"Option[${typeOf(wrapped)}]"
      case sc.OptionInputType(wrapped) =>
        t"Option[${typeOf(wrapped)}]"
      case sc.ListType(wrapped) =>
        t"List[${typeOf(wrapped)}]"
      case sc.ListInputType(wrapped) =>
        t"List[${typeOf(wrapped)}]"
      case tpe: sc.ScalarType[_] if tpe == sc.IDType =>
        Type.Name("ID")
      case tpe: sc.Type =>
        genType(tpe)
    }
    typeOf(field.tpe)
  }

  private[this] def fieldType(field: tree.Field, prefix: String = ""): Type = {
    generateFieldType(field) { tpe =>
      field match {
        case _: tree.CompositeField =>
          Type.Name(prefix + "." + field.name.capitalize)

        case _: tree.SimpleField =>
          Type.Name(tpe.namedType.name)
      }
    }
  }

  private[this] def generateFieldParams(
    fields: Vector[tree.Field],
    prefix: String
  ): List[Term.Param] = {
    fields.toList.map { field =>
      val tpe = fieldType(field, prefix)
      termParam(field.name, tpe)
    }
  }

  private[this] def generateFieldStats(fields: Vector[tree.Field]): List[Stat] = {
    fields.toList.flatMap {
      case field: tree.CompositeField =>
        val fields = field.fields(field.tpe)

        List(
          q"""
            final case class ${Type.Name(field.name.capitalize)}(
              ..${generateFieldParams(fields, field.name)}
            )
          """,
          q"""
            object ${Term.Name(field.name.capitalize)} {
              ..${generateFieldStats(fields)}
            }
          """
        )

      case _: tree.SimpleField =>
        List.empty
    }
  }

  def generate(operation: tree.Operation): Defn.Object = {
    val fields = operation.underlying.fields(operation.underlying.tpe)

    q"""
      object ${Term.Name(s"${operation.name}Query")} {
        final case class Data(..${generateFieldParams(fields, "Data")})
        object Data { ..${generateFieldStats(fields)} }
      }
    """
  }
}
