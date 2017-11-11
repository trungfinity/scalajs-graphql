// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

import sangria.ast
import sangria.schema.{CompositeType, Schema, Type}

private[codegen] final class TypeQuery(
  schema: Schema[_, _],
  document: ast.Document,
  sourceFile: Option[File]
) {

  def findFragment(fragmentSpread: ast.FragmentSpread): Result[ast.FragmentDefinition] = {
    document.fragments
      .get(fragmentSpread.name)
      .toRight(FragmentNotFoundException(fragmentSpread, fragmentSpread.name, sourceFile))
  }

  def findType(node: ast.AstNode, name: String): Result[Type] = {
    schema.allTypes
      .get(name)
      .toRight(TypeNotFoundException(node, name, sourceFile))
  }

  def findCompositeType(node: ast.AstNode, name: String): Result[CompositeType[_]] = {
    for {
      tpe <- findType(node, name)
      compositeType <- tpe match {
        case compositeType: CompositeType[_] =>
          Right(compositeType)

        case _ =>
          val compositeTypeClass = classOf[CompositeType[_]]
          Left(ExpectedTypeNotFoundException(node, name, tpe, compositeTypeClass, sourceFile))
      }
    } yield compositeType: CompositeType[_]
  }
}
