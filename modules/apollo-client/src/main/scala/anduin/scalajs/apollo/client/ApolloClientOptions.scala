// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.apollo.client

import scala.scalajs.js

import anduin.scalajs.apollo.cache.ApolloCache
import anduin.scalajs.apollo.link.ApolloLink

final case class ApolloClientOptions(
  link: ApolloLink,
  cache: ApolloCache,
  ssrMode: js.UndefOr[Boolean]
)
