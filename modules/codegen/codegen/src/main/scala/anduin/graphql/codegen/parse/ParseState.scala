// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen.parse

import cats.kernel.Monoid
import sangria.schema.{InputType, Named}

final case class ParseState(
  inputNamedTypes: Set[InputType[_] with Named]
)

object ParseState {

  val Empty: ParseState = ParseState(Set.empty)

  implicit val parseStateMonoid: Monoid[ParseState] = new Monoid[ParseState] {
    final def empty: ParseState = Empty
    final def combine(x: ParseState, y: ParseState) = {
      ParseState(x.inputNamedTypes ++ y.inputNamedTypes)
    }
  }
}
