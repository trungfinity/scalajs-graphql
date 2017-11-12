// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

import scala.meta

import sangria.ast

// scalastyle:off underscore.import
import scala.meta.{Type => _, _}

import sangria.schema._
// scalastyle:on underscore.import

final class Generator(
  schema: Schema[_, _],
  document: ast.Document,
  sourceFile: Option[File]
) {

  private[this] val typeQuery = new TypeQuery(schema, document, sourceFile)

  private[this] def termParam(name: String, tpe: meta.Type): Term.Param = {
    Term.Param(List.empty, Term.Name(name), Some(tpe), None)
  }

  def generateFieldType(field: tree.Field)(genType: Type => meta.Type): meta.Type = {
    def typeOf(tpe: Type): meta.Type = tpe match {
      case OptionType(wrapped) =>
        t"Option[${typeOf(wrapped)}]"
      case OptionInputType(wrapped) =>
        t"Option[${typeOf(wrapped)}]"
      case ListType(wrapped) =>
        t"List[${typeOf(wrapped)}]"
      case ListInputType(wrapped) =>
        t"List[${typeOf(wrapped)}]"
      case tpe: ScalarType[_] if tpe == IDType =>
        meta.Type.Name("ID")
      case tpe: Type =>
        genType(tpe)
    }
    typeOf(field.tpe)
  }

  private[this] def fieldType(field: tree.Field, prefix: String = ""): meta.Type = {
    generateFieldType(field) { tpe =>
      field match {
        case _: tree.CompositeField =>
          meta.Type.Name(prefix + "." + field.name.capitalize)

        case _: tree.SimpleField =>
          meta.Type.Name(tpe.namedType.name)
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
            final case class ${meta.Type.Name(field.name.capitalize)}(
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
