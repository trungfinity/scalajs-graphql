// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

import sangria.ast

// scalastyle:off underscore.import
import cats.implicits._
import sangria.schema._
// scalastyle:on underscore.import

private[codegen] final class DocumentParser(
  schema: Schema[_, _],
  document: ast.Document,
  sourceFile: Option[File]
) {

  private[this] val traversal = new SchemaTraversal(schema, sourceFile)
  private[this] val lookup = new SchemaLookup(schema, sourceFile)

  private[this] def parseField(
    astField: ast.Field,
    conditionType: CompositeType[_]
  ): Result[tree.Fields] = {
    for {
      tpe <- traversal.currentType
      namedType <- traversal.currentNamedType

      field <- namedType match {
        case _: AbstractType =>
          for {
            compositeType <- traversal.currentCompositeType
            subfields <- parseSelections(astField.selections, compositeType)
          } yield {
            tree.CompositeField(astField.name, subfields, compositeType)
          }

        case objectType: ObjectType[_, _] =>
          for {
            subfields <- parseSelections(astField.selections, objectType)
          } yield {
            tree.CompositeField(astField.name, subfields, objectType)
          }

        case _ =>
          Right(tree.SimpleField(astField.name, tpe))
      }
    } yield {
      Map(conditionType -> Vector(field))
    }
  }

  private[this] def parseFragmentSpread(
    fragmentSpread: ast.FragmentSpread
  ): Result[tree.Fields] = {
    for {
      fragment <- document.fragments
        .get(fragmentSpread.name)
        .toRight(FragmentNotFoundException(fragmentSpread, fragmentSpread.name, sourceFile))

      fields <- traversal.scope(fragment) {
        for {
          conditionType <- lookup.findCompositeType(fragment, fragment.typeCondition.name)
          fields <- parseSelections(fragment.selections, conditionType)
        } yield fields
      }
    } yield fields
  }

  private[this] def parseInlineFragment(
    inlineFragment: ast.InlineFragment,
    conditionType: CompositeType[_]
  ): Result[tree.Fields] = {
    for {
      nextConditionType <- inlineFragment.typeCondition match {
        case Some(typeCondition) =>
          lookup.findCompositeType(inlineFragment, typeCondition.name)
        case None => Right(conditionType)
      }

      fields <- parseSelections(inlineFragment.selections, nextConditionType)
    } yield fields
  }

  private[this] def parseSelection(
    selection: ast.Selection,
    conditionType: CompositeType[_]
  ): Result[tree.Fields] = {
    traversal.scope(selection) {
      selection match {
        case astField: ast.Field =>
          parseField(astField, conditionType)

        case fragmentSpread: ast.FragmentSpread =>
          parseFragmentSpread(fragmentSpread)

        case inlineFragment: ast.InlineFragment =>
          parseInlineFragment(inlineFragment, conditionType)
      }
    }
  }

  private[this] def parseSelections(
    selections: Vector[ast.Selection],
    conditionType: CompositeType[_]
  ): Result[tree.Fields] = {
    selections.foldMapM(parseSelection(_, conditionType))
  }

  private[this] def parseOperation(
    astOperation: ast.OperationDefinition
  ): Result[tree.Operation] = {
    for {
      operationName <- astOperation.name.toRight {
        OperationNotNamedException(astOperation, sourceFile)
      }

      operation <- traversal.scope(astOperation) {
        for {
          objectType <- traversal.currentObjectType
          fields <- parseSelections(astOperation.selections, objectType)
        } yield {
          tree.Operation(
            operationName,
            astOperation.operationType,
            tree.CompositeField(objectType.name, fields, objectType)
          )
        }
      }
    } yield operation
  }

  def parse(): Result[Vector[tree.Operation]] = {
    document.operations.values.toVector.foldMapM[Result, Vector[tree.Operation]] { operation =>
      parseOperation(operation).map(Vector(_))
    }
  }
}