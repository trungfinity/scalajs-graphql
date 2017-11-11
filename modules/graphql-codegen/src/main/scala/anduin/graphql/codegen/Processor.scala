// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import sangria.ast

// scalastyle:off underscore.import
import cats.implicits._
import sangria.schema._
// scalastyle:on underscore.import

private[codegen] final class Processor(schema: Schema[_, _], document: ast.Document) {

  private[this] val typeInfo = new TypeInfo(schema)
  private[this] val typeQuery = new TypeQuery(schema, document)

  private[this] def processField(
    astField: ast.Field,
    container: CompositeType[_]
  ): Result[tree.Fields] = {
    for {
      tpe <- typeInfo.currentType
      namedType <- typeInfo.currentNamedType

      field <- namedType match {
        case _: AbstractType =>
          for {
            compositeType <- typeInfo.currentCompositeType
            subfields <- processSelections(astField.selections, compositeType)
          } yield {
            tree.CompositeField(compositeType.name, subfields)
          }

        case objectType: ObjectType[_, _] =>
          for {
            subfields <- processSelections(astField.selections, objectType)
          } yield {
            tree.CompositeField(objectType.name, subfields)
          }

        case _ =>
          Right(tree.SimpleField(astField.name, tpe))
      }
    } yield {
      Map(container -> Vector(field))
    }
  }

  private[this] def processFragmentSpread(
    fragmentSpread: ast.FragmentSpread
  ): Result[tree.Fields] = {
    for {
      fragment <- typeQuery.findFragment(fragmentSpread)
      fields <- typeInfo.scope(fragment) {
        for {
          conditionType <- typeQuery.findCompositeType(fragment, fragment.typeCondition.name)
          fields <- processSelections(fragment.selections, conditionType)
        } yield fields
      }
    } yield fields
  }

  private[this] def processInlineFragment(
    inlineFragment: ast.InlineFragment,
    container: CompositeType[_]
  ): Result[tree.Fields] = {
    for {
      conditionType <- inlineFragment.typeCondition match {
        case Some(typeCondition) => typeQuery.findCompositeType(inlineFragment, typeCondition.name)
        case None => Right(container)
      }

      fields <- processSelections(inlineFragment.selections, conditionType)
    } yield fields
  }

  private[this] def processSelection(
    selection: ast.Selection,
    container: CompositeType[_]
  ): Result[tree.Fields] = {
    typeInfo.scope(selection) {
      selection match {
        case astField: ast.Field =>
          processField(astField, container)

        case fragmentSpread: ast.FragmentSpread =>
          processFragmentSpread(fragmentSpread)

        case inlineFragment: ast.InlineFragment =>
          processInlineFragment(inlineFragment, container)
      }
    }
  }

  private[this] def processSelections(
    selections: Vector[ast.Selection],
    container: CompositeType[_]
  ): Result[tree.Fields] = {
    selections.foldMapM(processSelection(_, container))
  }

  private[this] def processOperation(
    astOperation: ast.OperationDefinition
  ): Result[tree.Operation] = {
    for {
      operationName <- astOperation.name.toRight(OperationNotNamedException(astOperation))
      operation <- typeInfo.scope(astOperation) {
        for {
          objectType <- typeInfo.currentObjectType
          fields <- processSelections(astOperation.selections, objectType)
        } yield {
          tree.Operation(
            operationName,
            astOperation.operationType,
            tree.CompositeField(objectType.name, fields)
          )
        }
      }
    } yield operation
  }

  def parse(): Result[Vector[tree.Operation]] = {
    document.operations.values.toVector.foldMapM[Result, Vector[tree.Operation]] {
      processOperation(_).map(Vector(_))
    }
  }
}
