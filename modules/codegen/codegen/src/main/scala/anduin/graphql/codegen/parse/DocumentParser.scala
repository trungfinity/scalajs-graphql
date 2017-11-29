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
    implicit document: ast.Document
  ): Result[tree.Subfields] = {
    for {
      fieldType <- schemaTraversal.currentOutputType
      fieldNamedType <- schemaTraversal.currentOutputNamedType

      field <- fieldNamedType match {
        case fieldNamedType: CompositeType[_] with OutputType[_] =>
          for {
            fieldPossibleTypes <- schemaLookup.findPossibleTypes(fieldNamedType, astField)
            fieldSubfields <- parseSelections(
              astField.selections,
              fieldPossibleTypes,
              SelectionScope(fieldNamedType, fieldPossibleTypes)
            )
          } yield {
            tree.CompositeField(
              astField.name,
              fieldType,
              fieldSubfields,
              fieldPossibleTypes.toVector
            )
          }

        case _: OutputType[_] =>
          Right(tree.SingleField(astField.name, fieldType))
      }
    } yield {
      if (possibleTypes >= scope.possibleTypes) {
        tree.Subfields(Vector(field), Map.empty)
      } else {
        tree.Subfields(
          Vector.empty,
          possibleTypes.map(_ -> Vector(field)).toMap
        )
      }
    }
  }

  private[this] def parseFragmentSpread(
    fragmentSpread: ast.FragmentSpread,
    possibleTypes: Set[ObjectType[_, _]],
    scope: SelectionScope
  )(
    implicit document: ast.Document
  ): Result[tree.Subfields] = {
    for {
      fragment <- document.fragments
        .get(fragmentSpread.name)
        .toRight(FragmentNotFoundException(fragmentSpread))

      fields <- schemaTraversal.scope[Result](fragment)(
        for {
          typeCondition <- schemaLookup.findCompositeType(fragment.typeCondition)
          narrowedPossibleTypes <- schemaLookup.narrowPossibleTypes(
            possibleTypes,
            typeCondition,
            fragment.typeCondition
          )

          fields <- parseSelections(fragment.selections, narrowedPossibleTypes, scope)
        } yield fields
      )
    } yield fields
  }

  private[this] def parseInlineFragment(
    inlineFragment: ast.InlineFragment,
    possibleTypes: Set[ObjectType[_, _]],
    scope: SelectionScope
  )(
    implicit document: ast.Document
  ): Result[tree.Subfields] = {
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
  ): Result[tree.Subfields] = {
    schemaTraversal.scope[Result](selection)(
      selection match {
        case field: ast.Field =>
          parseField(field, possibleTypes, scope)

        case fragmentSpread: ast.FragmentSpread =>
          parseFragmentSpread(fragmentSpread, possibleTypes, scope)

        case inlineFragment: ast.InlineFragment =>
          parseInlineFragment(inlineFragment, possibleTypes, scope)
      }
    )
  }

  private[this] def parseSelections(
    selections: Vector[ast.Selection],
    possibleTypes: Set[ObjectType[_, _]],
    scope: SelectionScope
  )(
    implicit document: ast.Document
  ): Result[tree.Subfields] = {
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

      operation <- schemaTraversal.scope[Result](astOperation)(
        for {
          variables <- parseVariables(astOperation.variables)

          tpe <- schemaTraversal.currentObjectType
          possibleTypes <- schemaLookup.findPossibleTypes(tpe, astOperation)

          subfields <- parseSelections(
            astOperation.selections,
            possibleTypes,
            SelectionScope(tpe, possibleTypes)
          )

          underlyingField <- FieldMerger.merge(
            tree.CompositeField(
              tpe.name,
              tpe,
              subfields,
              possibleTypes.toVector
            )
          )
        } yield {
          tree.Operation(
            operationName,
            astOperation.operationType,
            variables,
            underlyingField
          )
        }
      )
    } yield operation
  }

  private[this] def parseOperations(
    astOperations: Vector[ast.OperationDefinition]
  )(
    implicit document: ast.Document
  ): Result[Vector[tree.Operation]] = {
    astOperations
      .foldMapM[Result, Vector[tree.Operation]] {
        parseOperation(_).map(Vector(_))
      }
      .map(_.sortBy(_.name))
  }

  def parse(document: ast.Document): Result[Vector[tree.Operation]] = {
    parseOperations(document.operations.values.toVector)(document)
  }
}

private[codegen] object DocumentParser {

  private final case class SelectionScope(
    tpe: CompositeType[_] with OutputType[_],
    possibleTypes: Set[ObjectType[_, _]]
  )
}
