// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen.parse

import scala.util.Try

import sangria.ast
import sangria.schema.{CompositeType, InputType, Named, ObjectType, OutputType, Type}

private[parse] object Typecaster {

  def namedType(tpe: Type, node: Option[ast.AstNode]): Result[Type with Named] = {
    Try(tpe.namedType).toEither.left.map {
      NamedTypeNotAvailableException(tpe, node, _)
    }
  }

  def inputType(tpe: Type, node: Option[ast.AstNode]): Result[InputType[_]] = {
    tpe match {
      case inputType: InputType[_] => Right(inputType)
      case _ => Left(UnexpectedTypeException(tpe, classOf[InputType[_]], node))
    }
  }

  def outputType(tpe: Type, node: Option[ast.AstNode]): Result[OutputType[_]] = {
    tpe match {
      case outputType: OutputType[_] => Right(outputType)
      case _ => Left(UnexpectedTypeException(tpe, classOf[OutputType[_]], node))
    }
  }

  def compositeType(tpe: Type, node: Option[ast.AstNode]): Result[CompositeType[_]] = {
    tpe match {
      case compositeType: CompositeType[_] => Right(compositeType)
      case _ => Left(UnexpectedTypeException(tpe, classOf[CompositeType[_]], node))
    }
  }

  def objectType(tpe: Type, node: Option[ast.AstNode]): Result[ObjectType[_, _]] = {
    tpe match {
      case objectType: ObjectType[_, _] => Right(objectType)
      case _ => Left(UnexpectedTypeException(tpe, classOf[ObjectType[_, _]], node))
    }
  }
}
