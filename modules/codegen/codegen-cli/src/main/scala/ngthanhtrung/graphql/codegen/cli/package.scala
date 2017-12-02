// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.graphql.codegen

package object cli {
  private[cli] type Result[A] = Either[Throwable, A]
}
