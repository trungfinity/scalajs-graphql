// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen2

import sangria.ast

// scalastyle:off underscore.import
import cats.implicits._
import sangria.schema._
// scalastyle:on underscore.import

private[codegen2] final class DocumentParser(
  schema: Schema[_, _]
) {

  import DocumentParser._ // scalastyle:ignore import.grouping underscore.import

  private[this] val schemaTraversal = new SchemaTraversal(schema)
  private[this] val schemaLookup = new SchemaLookup(schema)

  private[this] def parseField(
    astField: ast.Field,
    possibleTypes: Set[ObjectType[_, _]],
    scope: SelectionScope
  )(
    implicit document: ast.Document,
    sourceFile: Option[SourceFile]
  ): Result[tree.Fields] = {
    for {
      fieldType <- schemaTraversal.currentType
      fieldNamedType <- schemaTraversal.currentNamedType

      field <- fieldNamedType match {
        case fieldCompositeType: CompositeType[_] =>
          for {
            fieldPossibleTypes <- schemaLookup.findPossibleTypes(fieldCompositeType, astField)
            subfields <- parseSelections(
              astField.selections,
              fieldPossibleTypes,
              SelectionScope(fieldCompositeType, fieldPossibleTypes)
            )
          } yield {
            tree.CompositeField(astField, subfields, fieldCompositeType)
          }

        case _ =>
          Right(tree.SingleField(astField, fieldType))
      }
    } yield {
      if (possibleTypes >= scope.possibleTypes) {
        Map(scope.tpe -> Vector(field))
      } else {
        possibleTypes.map(_ -> Vector(field)).toMap
      }
    }
  }

  private[this] def parseFragmentSpread(
    fragmentSpread: ast.FragmentSpread,
    possibleTypes: Set[ObjectType[_, _]],
    scope: SelectionScope
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
          typeCondition <- schemaLookup.findCompositeType(fragment.typeCondition)
          narrowedPossibleTypes <- schemaLookup.narrowPossibleTypes(
            possibleTypes,
            typeCondition,
            fragment.typeCondition
          )

          fields <- parseSelections(fragment.selections, narrowedPossibleTypes, scope)
        } yield fields
      }
    } yield fields
  }

  private[this] def parseInlineFragment(
    inlineFragment: ast.InlineFragment,
    possibleTypes: Set[ObjectType[_, _]],
    scope: SelectionScope
  )(
    implicit document: ast.Document,
    sourceFile: Option[SourceFile]
  ): Result[tree.Fields] = {
    for {
      narrowedPossibleTypes <- inlineFragment.typeCondition match {
        case Some(namedType) =>
          for {
            typeCondition <- schemaLookup.findCompositeType(namedType)
            narrowedPossibleTypes <- schemaLookup.narrowPossibleTypes(
              possibleTypes,
              typeCondition,
              namedType
            )
          } yield narrowedPossibleTypes

        case None =>
          Right(possibleTypes)
      }

      fields <- parseSelections(inlineFragment.selections, narrowedPossibleTypes, scope)
    } yield fields
  }

  private[this] def parseSelection(
    selection: ast.Selection,
    possibleTypes: Set[ObjectType[_, _]],
    scope: SelectionScope
  )(
    implicit document: ast.Document,
    sourceFile: Option[SourceFile]
  ): Result[tree.Fields] = {
    schemaTraversal.scope(selection) {
      selection match {
        case field: ast.Field =>
          parseField(field, possibleTypes, scope)

        case fragmentSpread: ast.FragmentSpread =>
          parseFragmentSpread(fragmentSpread, possibleTypes, scope)

        case inlineFragment: ast.InlineFragment =>
          parseInlineFragment(inlineFragment, possibleTypes, scope)
      }
    }
  }

  private[this] def parseSelections(
    selections: Vector[ast.Selection],
    possibleTypes: Set[ObjectType[_, _]],
    scope: SelectionScope
  )(
    implicit document: ast.Document,
    sourceFile: Option[SourceFile]
  ): Result[tree.Fields] = {
    for {
      fields <- selections.foldMapM(parseSelection(_, possibleTypes, scope))
    } yield fields
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
          subfields <- parseSelections(
            astOperation.selections,
            possibleTypes,
            SelectionScope(objectType, possibleTypes)
          )
        } yield {
          // Create a dummy field node for this operation
          val underlyingFieldNode = ast.Field(
            alias = None,
            name = "data",
            arguments = Vector.empty,
            directives = astOperation.directives,
            selections = astOperation.selections,
            comments = astOperation.comments,
            trailingComments = astOperation.trailingComments,
            position = astOperation.position
          )

          tree.Operation(
            operationName,
            astOperation.operationType,
            tree.CompositeField(underlyingFieldNode, subfields, objectType)
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

private[codegen2] object DocumentParser {

  private final case class SelectionScope(
    tpe: CompositeType[_],
    possibleTypes: Set[ObjectType[_, _]]
  )
}
