// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen2

import sangria.ast

// scalastyle:off underscore.import
import cats.implicits._
import sangria.schema._
// scalastyle:on underscore.import

private[codegen2] final class DocumentParser(
  schemaTraversal: SchemaTraversal,
  schemaLookup: SchemaLookup
) {

  private[this] def parseField(
    astField: ast.Field,
    possibleTypes: Set[ObjectType[_, _]]
  )(
    implicit document: ast.Document,
    sourceFile: Option[SourceFile]
  ): Result[tree.Fields] = {
    for {
      tpe <- schemaTraversal.currentType
      namedType <- schemaTraversal.currentNamedType

      field <- namedType match {
        case compositeType: CompositeType[_] =>
          for {
            fieldPossibleTypes <- schemaLookup.findPossibleTypes(compositeType, astField)
            subfields <- parseSelections(astField.selections, fieldPossibleTypes)
          } yield {
            tree.CompositeField(astField.name, subfields, compositeType, fieldPossibleTypes)
          }

        case _ =>
          Right(tree.SingleField(astField.name, tpe))
      }
    } yield {
      possibleTypes.map(_ -> Vector(field)).toMap
    }
  }

  private[this] def parseFragmentSpread(
    fragmentSpread: ast.FragmentSpread,
    possibleTypes: Set[ObjectType[_, _]]
  )(
    implicit document: ast.Document,
    sourceFile: Option[SourceFile]
  ): Result[tree.Fields] = {
    for {
      fragment <- document.fragments
        .get(fragmentSpread.name)
        .toRight(FragmentNotFoundException(fragmentSpread))

      fields <- schemaTraversal.scope(fragment) {
        for {
          compositeConditionType <- schemaLookup.findCompositeType(fragment.typeCondition)
          narrowedPossibleTypes <- schemaLookup.narrowPossibleTypes(
            possibleTypes,
            compositeConditionType,
            fragment.typeCondition
          )

          fields <- parseSelections(fragment.selections, narrowedPossibleTypes)
        } yield fields
      }
    } yield fields
  }

  private[this] def parseInlineFragment(
    inlineFragment: ast.InlineFragment,
    possibleTypes: Set[ObjectType[_, _]]
  )(
    implicit document: ast.Document,
    sourceFile: Option[SourceFile]
  ): Result[tree.Fields] = {
    for {
      narrowedPossibleTypes <- inlineFragment.typeCondition match {
        case Some(typeCondition) =>
          for {
            compositeConditionType <- schemaLookup.findCompositeType(typeCondition)
            narrowedPossibleTypes <- schemaLookup.narrowPossibleTypes(
              possibleTypes,
              compositeConditionType,
              typeCondition
            )
          } yield narrowedPossibleTypes

        case None =>
          Right(possibleTypes)
      }

      fields <- parseSelections(inlineFragment.selections, narrowedPossibleTypes)
    } yield fields
  }

  private[this] def parseSelection(
    selection: ast.Selection,
    possibleTypes: Set[ObjectType[_, _]]
  )(
    implicit document: ast.Document,
    sourceFile: Option[SourceFile]
  ): Result[tree.Fields] = {
    schemaTraversal.scope(selection) {
      selection match {
        case field: ast.Field =>
          parseField(field, possibleTypes)

        case fragmentSpread: ast.FragmentSpread =>
          parseFragmentSpread(fragmentSpread, possibleTypes)

        case inlineFragment: ast.InlineFragment =>
          parseInlineFragment(inlineFragment, possibleTypes)
      }
    }
  }

  private[this] def parseSelections(
    selections: Vector[ast.Selection],
    possibleTypes: Set[ObjectType[_, _]]
  )(
    implicit document: ast.Document,
    sourceFile: Option[SourceFile]
  ): Result[tree.Fields] = {
    selections.foldMapM(parseSelection(_, possibleTypes))
  }

  private[this] def parseOperation(
    astOperation: ast.OperationDefinition
  )(
    implicit document: ast.Document,
    sourceFile: Option[SourceFile]
  ): Result[tree.Operation] = {
    for {
      operationName <- astOperation.name.toRight {
        OperationNotNamedException(astOperation)
      }

      operation <- schemaTraversal.scope(astOperation) {
        for {
          objectType <- schemaTraversal.currentObjectType
          possibleTypes <- schemaLookup.findPossibleTypes(objectType, astOperation)
          subfields <- parseSelections(astOperation.selections, possibleTypes)
        } yield {
          tree.Operation(
            operationName,
            astOperation.operationType,
            tree.CompositeField("data", subfields, objectType, possibleTypes)
          )
        }
      }
    } yield operation
  }

  def parse(document: ast.Document)(
    implicit sourceFile: Option[SourceFile]
  ): Result[Vector[tree.Operation]] = {
    document.operations.values.toVector.foldMapM[Result, Vector[tree.Operation]] { operation =>
      parseOperation(operation)(document, sourceFile).map(Vector(_))
    }
  }
}
