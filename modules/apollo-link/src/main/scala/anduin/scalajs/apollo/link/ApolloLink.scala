// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.apollo.link

trait ApolloLink {
  type Raw <: internal.ApolloLink
  val raw: Raw
}
