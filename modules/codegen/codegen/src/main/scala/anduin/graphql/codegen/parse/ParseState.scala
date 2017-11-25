// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen.parse

import cats.kernel.Monoid

import anduin.graphql.codegen.tree

final case class ParseState(
  fragments: Set[tree.Fragment],
  inputTypes: Set[tree.InputType]
) {

  def addInputType(inputType: tree.InputType): ParseState = {
    copy(inputTypes = inputTypes + inputType)
  }
}

object ParseState {

  implicit val parseStateMonoid: Monoid[ParseState] = new Monoid[ParseState] {

    final def empty: ParseState = {
      ParseState(
        fragments = Set.empty,
        inputTypes = Set.empty
      )
    }

    final def combine(x: ParseState, y: ParseState) = {
      ParseState(
        x.fragments ++ y.fragments,
        x.inputTypes ++ y.inputTypes
      )
    }
  }
}
