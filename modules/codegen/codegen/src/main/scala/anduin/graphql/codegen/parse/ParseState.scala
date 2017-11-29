// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen.parse

import cats.kernel.Monoid
import sangria.schema.InputType

final case class ParseState(
  inputTypes: Set[InputType[_]]
)

object ParseState {

  implicit val parseStateMonoid: Monoid[ParseState] = new Monoid[ParseState] {
    final def empty: ParseState = ParseState(Set.empty)
    final def combine(x: ParseState, y: ParseState) = ParseState(x.inputTypes ++ y.inputTypes)
  }
}
