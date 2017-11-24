// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

package object parse {
  type Result[A] = Either[CodegenException, A]
}
