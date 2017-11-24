// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen.parse

import sangria.{ast, schema}

import anduin.graphql.codegen.tree

private[codegen] sealed abstract class CodegenException extends RuntimeException {
  def message: String
  def cause: Throwable = null // scalastyle:ignore null
  final override def getMessage: String = message
  final override def getCause: Throwable = cause
  final override def fillInStackTrace(): Throwable = this
}

private[codegen] sealed abstract class ParseError extends CodegenException

private[codegen] final case class OperationNotNamedError(
  operation: ast.OperationDefinition
) extends ParseError {
  def message: String = "Operation must be named."
}

private[codegen] sealed abstract class ParseException extends CodegenException

private[codegen] case object EmptyNodeStackException extends ParseException {
  def message: String = "AST node stack is empty."
}

private[codegen] final case class TypeNotAvailableException(
  node: ast.AstNode
) extends ParseException {
  def message: String = s"AST node $node does not have a corresponding type."
}

private[codegen] final case class NamedTypeNotAvailableException(
  tpe: schema.Type,
  node: ast.AstNode,
  override val cause: Throwable
) extends ParseException {
  def message: String = s"AST node $node has type $tpe, expected to contain a named type."
}

private[codegen] final case class UnexpectedTypeException(
  tpe: schema.Type,
  expectedType: Class[_ <: schema.Type],
  node: ast.AstNode
) extends ParseException {
  def message: String = s"AST node $node has type $tpe, but expected $expectedType."
}

private[codegen] final case class TypeNotFoundException(
  namedType: ast.NamedType
) extends ParseException {
  def message: String = s"""Cannot find a type with name "${namedType.name}"."""
}

private[codegen] final case class InputTypeNotFoundException(
  tpe: ast.Type
) extends ParseException {
  def message: String = s"""Cannot find an input type with name "${tpe.namedType.name}"."""
}

private[codegen] final case class FragmentNotFoundException(
  fragmentSpread: ast.FragmentSpread
) extends ParseException {
  def message: String = s"""Cannot find a fragment with name "${fragmentSpread.name}"."""
}

private[codegen] final case class PossibleTypesUnavailableException(
  tpe: schema.AbstractType,
  node: ast.AstNode
) extends ParseException {
  def message: String = s"""Cannot find possible types for abstract type with name "${tpe.name}"."""
}

private[codegen] final case class ConflictedFieldsException(
  firstField: tree.Field,
  secondField: tree.Field
) extends ParseException {
  def message: String = s"Cannot merge 2 conflicted fields $firstField and $secondField."
}
