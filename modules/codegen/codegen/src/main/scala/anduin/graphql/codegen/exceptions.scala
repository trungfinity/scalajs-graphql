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
  final def message: String = s"Line $lineString, column $columnString: $details"
}

final case class OperationNotNamedException(
  operation: ast.OperationDefinition
) extends CodegenUserException {
  def position: Option[Position] = operation.position
  def details: String = "Operation must be named."
}

sealed abstract class CodegenSystemException extends CodegenException {

  final def message: String = {
    s"Line $lineString, column $columnString: $details" +
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
  tpe: schema.Type,
  node: ast.AstNode,
  override val cause: Throwable
) extends CodegenSystemException {
  def position: Option[Position] = node.position
  def details: String = s"AST node $node has type $tpe, expected a named type."
}

final case class UnexpectedTypeException(
  tpe: schema.Type,
  expectedType: Class[_ <: schema.Type],
  node: ast.AstNode
) extends CodegenSystemException {
  def position: Option[Position] = node.position
  def details: String = s"AST node $node has type $tpe, but expected $expectedType."
}

final case class TypeNotFoundException(
  namedType: ast.NamedType
) extends CodegenSystemException {
  def position: Option[Position] = namedType.position
  def details: String = s"""Cannot find a type with name "${namedType.name}"."""
}

final case class InputTypeNotFoundException(
  tpe: ast.Type
) extends CodegenSystemException {
  def position: Option[Position] = tpe.position
  def details: String = s"""Cannot find an input type with name "${tpe.namedType.name}"."""
}

final case class FragmentNotFoundException(
  fragmentSpread: ast.FragmentSpread
) extends CodegenSystemException {
  def position: Option[Position] = fragmentSpread.position
  def details: String = s"""Cannot find a fragment with name "${fragmentSpread.name}"."""
}

final case class PossibleTypesUnavailableException(
  tpe: schema.AbstractType,
  node: ast.AstNode
) extends CodegenSystemException {
  def position: Option[Position] = node.position
  def details: String = s"""Cannot find possible types for abstract type with name "${tpe.name}"."""
}

final case class ConflictedFieldsException(
  firstField: tree.Field,
  secondField: tree.Field
) extends CodegenSystemException {
  def position: Option[Position] = secondField.node.position
  def details: String = s"Cannot merge 2 conflicted fields $firstField and $secondField."
}
