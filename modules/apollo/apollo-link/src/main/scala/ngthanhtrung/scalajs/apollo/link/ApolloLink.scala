// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.scalajs.apollo.link

trait ApolloLink {
  type Raw <: internal.ApolloLink
  val raw: Raw
}
