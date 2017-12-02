package ngthanhtrung.graphql.codegen.parse

import sangria.{ast, schema}

import ngthanhtrung.graphql.codegen.tree

private[codegen] sealed abstract class ParseException extends RuntimeException {
  def message: String
  def cause: Throwable = null // scalastyle:ignore null
  final override def getMessage: String = message
  final override def getCause: Throwable = cause
  final override def fillInStackTrace(): Throwable = this
}

private[codegen] final case class UserErrorException(
  error: ParseError
) extends ParseException {
  def message: String = "There is a user error in the parse input."
  override def cause: Throwable = error
}

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
  node: Option[ast.AstNode],
  override val cause: Throwable
) extends ParseException {

  def message: String = {
    node.fold(
      s"$tpe is expected to contain a named type."
    ) { node =>
      s"AST node $node has type $tpe, expected to contain a named type."
    }
  }
}

private[codegen] final case class UnexpectedTypeException(
  tpe: schema.Type,
  expectedType: Class[_],
  node: Option[ast.AstNode]
) extends ParseException {

  def message: String = {
    node.fold(
      s"$tpe is expected to be $expectedType."
    ) { node =>
      s"AST node $node has type $tpe, but expected $expectedType."
    }
  }
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
