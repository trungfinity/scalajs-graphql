// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql

package object codegen2 {
  type Result[A] = Either[CodegenException, A]
}
