// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.graphql.codegen.parse

import scala.util.control.NoStackTrace

import org.parboiled2.Position
import sangria.ast

private[codegen] sealed abstract class ParseError extends NoStackTrace {
  def message: String
  def position: Option[Position]
  final override def getMessage: String = message
}

private[codegen] final case class OperationNotNamedError(
  operation: ast.OperationDefinition
) extends ParseError {
  def message: String = "Operation must be named."
  def position: Option[Position] = operation.position
}
