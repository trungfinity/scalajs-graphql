// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen.parse

import sangria.ast

import anduin.graphql.codegen.tree

// scalastyle:off underscore.import
import cats.implicits._
import sangria.schema._
// scalastyle:on underscore.import

private[codegen] final class DocumentParser(
  schema: Schema[_, _]
) {

  import DocumentParser._ // scalastyle:ignore import.grouping underscore.import

  private[this] val schemaTraversal = new SchemaTraversal(schema)
  private[this] val schemaLookup = new SchemaLookup(schema)

  private[this] def parseVariable(
    astVariable: ast.VariableDefinition
  ): Result[tree.Variable] = {
    for {
      tpe <- schemaLookup.findInputType(astVariable.tpe)
    } yield tree.Variable(astVariable.name, tpe)
  }

  private[this] def parseVariables(
    astVariables: Vector[ast.VariableDefinition]
  ): Result[Vector[tree.Variable]] = {
    astVariables.foldMapM[Result, Vector[tree.Variable]] { astVariable =>
      parseVariable(astVariable).map(Vector(_))
    }
  }

  private[this] def parseField(
    astField: ast.Field,
    possibleTypes: Set[ObjectType[_, _]],
    scope: SelectionScope
  )(
    implicit document: ast.Document,
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
            tree.CompositeField(astField, subfields, fieldCompositeType, fieldPossibleTypes)
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
    implicit document: ast.Document
  ): Result[tree.Fields] = {
    for {
      fragment <- document.fragments
        .get(fragmentSpread.name)
        .toRight(FragmentNotFoundException(fragmentSpread))

      fields <- schemaTraversal.scope[Result](fragment) {
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
    implicit document: ast.Document
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
    implicit document: ast.Document
  ): Result[tree.Fields] = {
    schemaTraversal.scope[Result](selection) {
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
    implicit document: ast.Document
  ): Result[tree.Fields] = {
    selections.foldMapM(parseSelection(_, possibleTypes, scope))
  }

  private[this] def parseOperation( // scalastyle:ignore method.length
    astOperation: ast.OperationDefinition
  )(
    implicit document: ast.Document
  ): Result[tree.Operation] = {
    for {
      operationName <- astOperation.name.toRight(
        UserErrorException(OperationNotNamedError(astOperation))
      )

      operation <- schemaTraversal.scope[Result](astOperation) {
        for {
          variables <- parseVariables(astOperation.variables)

          objectType <- schemaTraversal.currentObjectType
          possibleTypes <- schemaLookup.findPossibleTypes(objectType, astOperation)

          subfields <- parseSelections(
            astOperation.selections,
            possibleTypes,
            SelectionScope(objectType, possibleTypes)
          )

          underlyingField <- {
            // Create a dummy field node for this operation
            // In particular this pattern should be eliminated
            val node = ast.Field(
              alias = None,
              name = "data",
              arguments = Vector.empty,
              directives = astOperation.directives,
              selections = astOperation.selections,
              comments = astOperation.comments,
              trailingComments = astOperation.trailingComments,
              position = astOperation.position
            )

            FieldMerger.merge(tree.CompositeField(node, subfields, objectType, possibleTypes))
          }
        } yield {
          tree.Operation(
            operationName,
            astOperation.operationType,
            variables,
            underlyingField
          )
        }
      }
    } yield operation
  }

  private[this] def parseOperations(
    astOperations: Vector[ast.OperationDefinition]
  )(
    implicit document: ast.Document
  ): Result[Vector[tree.Operation]] = {
    astOperations
      .foldMapM[Result, Vector[tree.Operation]] { operation =>
        parseOperation(operation).map(Vector(_))
      }
      .map(_.sortBy(_.name))
  }

  def parse(document: ast.Document): Result[Vector[tree.Operation]] = {
    parseOperations(document.operations.values.toVector)(document)
  }
}

private[codegen] object DocumentParser {

  private final case class SelectionScope(
    tpe: CompositeType[_],
    possibleTypes: Set[ObjectType[_, _]]
  )
}
