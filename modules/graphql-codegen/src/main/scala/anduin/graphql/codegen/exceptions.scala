// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import org.parboiled2.Position
import sangria.{ast, schema}

import anduin.exception.BaseException

sealed abstract class CodegenException extends BaseException {

  def position: Option[Position]
  protected def lineString: String = position.fold("?")(_.line.toString)
  protected def columnString: String = position.fold("?")(_.column.toString)

  def details: String
}

sealed abstract class CodegenUserException extends CodegenException {
  final def message: String = s"[$lineString:$columnString] $details"
}

final case class OperationNotNamedException(
  operation: ast.OperationDefinition
) extends CodegenUserException {
  def position: Option[Position] = operation.position
  def details: String = "Operation must be named."
}

sealed abstract class CodegenSystemException extends CodegenException {

  final def message: String = {
    s"[$lineString:$columnString] $details." +
      "\n\nThis is likely an error from the code generator itself" +
      " which is not expected to happen." +
      " Please include the stack trace and report the issue at" +
      " https://github.com/anduintransaction/scala-graphql/issues"
  }
}

case object EmptyNodeStackException extends CodegenSystemException {
  def position: Option[Position] = None
  def details: String = "AST node stack is empty."
}

final case class TypeNotAvailableException(
  node: ast.AstNode
) extends CodegenSystemException {
  def position: Option[Position] = node.position
  def details: String = s"AST node $node does have a corresponding type."
}

final case class NamedTypeNotAvailableException(
  node: ast.AstNode,
  tpe: schema.Type,
  override val cause: Throwable
) extends CodegenSystemException {
  def position: Option[Position] = node.position
  def details: String = s"AST node $node has type $tpe, expected a named type."
}

final case class UnexpectedTypeException(
  node: ast.AstNode,
  tpe: schema.Type,
  expectedType: Class[_ <: schema.Type]
) extends CodegenSystemException {
  def position: Option[Position] = node.position
  def details: String = s"AST node $node has type $tpe, but expected $expectedType."
}

final case class TypeNotFoundException(
  node: ast.AstNode,
  name: String
) extends CodegenSystemException {
  def position: Option[Position] = node.position
  def details: String = s"""Cannot find a type with name "$name"."""
}

final case class ExpectedTypeNotFoundException(
  node: ast.AstNode,
  name: String,
  tpe: schema.Type,
  expectedType: Class[_ <: schema.Type]
) extends CodegenSystemException {
  def position: Option[Position] = node.position

  def details: String = {
    s"""Type with name "$name" was found: $tpe, but expected $expectedType."""
  }
}

final case class FragmentNotFoundException(
  node: ast.AstNode,
  name: String
) extends CodegenSystemException {
  def position: Option[Position] = node.position
  def details: String = s"""Cannot find a fragment with name "$name"."""
}
