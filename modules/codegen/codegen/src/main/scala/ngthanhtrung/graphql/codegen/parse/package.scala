// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.graphql.codegen

import cats.data.StateT

package object parse {

  type Result[A] = Either[ParseException, A]

  object Result {
    type WithState[A] = StateT[Result, ParseState, A]
  }
}
