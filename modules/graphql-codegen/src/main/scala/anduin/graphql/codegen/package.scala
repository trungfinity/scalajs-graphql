// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql

package object codegen {
  type Result[A] = Either[CodegenException, A]
}
