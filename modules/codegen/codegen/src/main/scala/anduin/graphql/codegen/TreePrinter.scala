// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import sangria.ast

// scalastyle:off underscore.import
import sangria.schema._
// scalastyle:on underscore.import

private[codegen] object TreePrinter {

  private val Space = " "
  private val NewLine = "\n"

  private def printType(tpe: Type, builder: StringBuilder): Unit = {
    tpe match {
      case OptionType(inner) =>
        builder ++= "Option of "
        printType(inner, builder)

      case OptionInputType(inner) =>
        builder ++= "Option of "
        printType(inner, builder)

      case ListType(inner) =>
        builder ++= "List of "
        printType(inner, builder)

      case ListInputType(inner) =>
        builder ++= "List of "
        printType(inner, builder)

      case IDType =>
        builder ++= "ID"

      case _ =>
        builder ++= tpe.namedType.name
    }
  }

  private def printVariable(variable: tree.Variable, builder: StringBuilder): Unit = {
    builder ++= "$"
    builder ++= variable.name
    builder ++= " -> "
    printType(variable.tpe, builder)
    builder ++= NewLine
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
        printSubFields(compositeField.subfields, builder, indentation + 2)
    }
  }

  private def printSubFields(
    subfields: tree.Subfields,
    builder: StringBuilder,
    indentation: Int = 0
  ): Unit = {
    subfields.base.foreach { field =>
      builder ++= (Space * indentation)
      printField(field, builder, indentation)
    }

    subfields.projections.foreach {
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

    operation.variables.foreach(printVariable(_, builder))

    builder ++= (Space * (indentation + 2))
    printField(operation.underlyingField, builder, indentation + 2)
  }

  def print(tree: anduin.graphql.codegen.tree.Tree): String = {
    import anduin.graphql.codegen.tree._ // scalastyle:ignore import.grouping underscore.import

    val builder = StringBuilder.newBuilder

    tree match {
      case operation: Operation => printOperation(operation, builder)
      case variable: Variable => printVariable(variable, builder)
      case field: Field => printField(field, builder)
    }

    builder.result()
  }
}
