// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen2

import sangria.ast

// scalastyle:off underscore.import
import sangria.schema._
// scalastyle:on underscore.import

private[codegen2] object TreePrinter {

  private val Space = " "
  private val NewLine = "\n"

  private def printType(tpe: Type, builder: StringBuilder): Unit = {
    tpe match {
      case OptionType(innerTpe) =>
        builder ++= "Option of "
        printType(innerTpe, builder)

      case OptionInputType(innerTpe) =>
        builder ++= "Option of "
        printType(innerTpe, builder)

      case ListType(innerTpe) =>
        builder ++= "List of "
        printType(innerTpe, builder)

      case ListInputType(innerTpe) =>
        builder ++= "List of "
        printType(innerTpe, builder)

      case IDType =>
        builder ++= "ID"

      case _ =>
        builder ++= tpe.namedType.name
    }
  }

  private def printField(
    field: tree.Field,
    builder: StringBuilder,
    indentation: Int = 0
  ): Unit = {
    builder ++= field.name

    field match {
      case singleField: tree.SingleField =>
        builder ++= " -> "
        printType(singleField.tpe, builder)
        builder ++= NewLine

      case compositeField: tree.CompositeField =>
        builder ++= NewLine
        printFields(compositeField.subfields, builder, indentation + 2)
    }
  }

  private def printFields(
    fields: tree.Fields,
    builder: StringBuilder,
    indentation: Int = 0
  ): Unit = {
    fields.foreach {
      case (container, fieldsByContainer) =>
        fieldsByContainer.foreach { field =>
          builder ++= (Space * indentation)
          builder ++= container.name
          builder ++= ": "
          printField(field, builder, indentation)
        }
    }
  }

  private def printOperation(
    operation: tree.Operation,
    builder: StringBuilder,
    indentation: Int = 0
  ): Unit = {
    builder ++= (operation.operationType match {
      case ast.OperationType.Query => "Query"
      case ast.OperationType.Mutation => "Mutation"
      case ast.OperationType.Subscription => "Subscription"
    })

    builder ++= Space
    builder ++= operation.name
    builder ++= NewLine

    builder ++= (Space * (indentation + 2))
    printField(operation.underlyingField, builder, indentation + 2)
  }

  def print(t: tree.Tree): String = {
    val builder = StringBuilder.newBuilder

    t match {
      case operation: tree.Operation => printOperation(operation, builder)
      case field: tree.Field => printField(field, builder)
    }

    builder.result()
  }
}
