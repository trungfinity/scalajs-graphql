// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen.parse

import cats.data.StateT
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

  private[this] def parseInputType(tpe: InputType[_]): Result.WithState[Unit] = {
    for {
      namedType <- StateT.lift(Typecaster.namedType(tpe, node = None))
      inputNamedType <- StateT.lift(
        Typecaster
          .inputType(namedType, node = None)
          .map(_.asInstanceOf[InputType[_] with Named]) // scalastyle:ignore token
      )

      _ <- StateT[Result, ParseState, Unit] { state =>
        def modified(modifiedState: ParseState) = Right((modifiedState, ()))
        lazy val ignored = modified(state)

        if (!state.inputNamedTypes.contains(inputNamedType)) {
          inputNamedType match {
            case _: ScalarType[_] | _: ScalarAlias[_, _] =>
              ignored

            case enumType: EnumType[_] =>
              modified(state.copy(inputNamedTypes = state.inputNamedTypes + enumType))

            case _: ListInputType[_] | _: OptionInputType[_] =>
              Left(UnexpectedTypeException(inputNamedType, classOf[Named], node = None))

            case inputObjectType: InputObjectType[_] =>
              val modifiedState = state.copy(
                inputNamedTypes = state.inputNamedTypes + inputObjectType
              )

              inputObjectType.fields
                .traverse[Result.WithState, Unit] { field =>
                  parseInputType(field.fieldType)
                }
                .void
                .run(modifiedState)
          }
        } else {
          ignored
        }
      }
    } yield ()
  }

  private[this] def parseVariable(
    variable: ast.VariableDefinition
  ): Result.WithState[tree.Variable] = {
    for {
      tpe <- StateT.lift(schemaLookup.findInputType(variable.tpe))
      _ <- parseInputType(tpe)
    } yield tree.Variable(variable.name, tpe)
  }

  private[this] def parseVariables(
    astVariables: Vector[ast.VariableDefinition]
  ): Result.WithState[Vector[tree.Variable]] = {
    astVariables.foldMapM[Result.WithState, Vector[tree.Variable]] { astVariable =>
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
        case fieldNamedType: CompositeType[_] =>
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
  ): Result.WithState[tree.Operation] = {
    for {
      operationName <- StateT.lift(
        astOperation.name.toRight(
          UserErrorException(OperationNotNamedError(astOperation))
        ): Result[String]
      )

      operation <- schemaTraversal.scope[Result.WithState](astOperation)(
        for {
          variables <- parseVariables(astOperation.variables)

          tpe <- StateT.lift(schemaTraversal.currentObjectType)
          possibleTypes <- StateT.lift(schemaLookup.findPossibleTypes(tpe, astOperation))

          subfields <- StateT.lift(
            parseSelections(
              astOperation.selections,
              possibleTypes,
              SelectionScope(tpe, possibleTypes)
            )
          )

          underlyingField <- StateT.lift[Result, ParseState, tree.CompositeField](
            FieldMerger.merge(
              tree.CompositeField(
                tpe.name,
                tpe,
                subfields,
                possibleTypes.toVector
              )
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
  ): Result.WithState[Vector[tree.Operation]] = {
    astOperations
      .foldMapM[Result.WithState, Vector[tree.Operation]] {
        parseOperation(_).map(Vector(_))
      }
      .map(_.sortBy(_.name))
  }

  def parse(document: ast.Document): Result.WithState[Vector[tree.Operation]] = {
    parseOperations(document.operations.values.toVector)(document)
  }
}

private[codegen] object DocumentParser {

  private final case class SelectionScope(
    tpe: CompositeType[_],
    possibleTypes: Set[ObjectType[_, _]]
  )
}
