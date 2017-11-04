// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo

import scala.scalajs.js

import japgolly.scalajs.react.raw.{React, ReactClassUntyped, ReactDOMServer}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

import anduin.scalajs.apollo.cache.internal.{ApolloInMemoryCache, ApolloInMemoryCacheOptions}
import anduin.scalajs.apollo.client.internal.{ApolloClient, ApolloClientOptions}
import anduin.scalajs.apollo.graphqltools.internal.{
  ExecutableSchemaOptions,
  GraphqlTools,
  SchemaMockFunctionsOptions
}
import anduin.scalajs.apollo.link.internal.{ApolloMockLink, ApolloMockLinkOptions}

// scalastyle:off underscore.import
import japgolly.scalajs.react.vdom.html_<^._
// scalastyle:on underscore.import

final class ReactApolloSpec extends FlatSpec with BeforeAndAfterAll {

  private[this] final class User(val id: Int, val name: String) extends js.Object
  private[this] final class Data(val user: js.UndefOr[User]) extends js.Object
  private[this] final class Props(val data: Data) extends js.Object
  private[this] final class Vars(val id: Int) extends js.Object

  private[this] val component = { props: Props =>
    props.data.user
      .fold {
        <.div("Loading")
      } { user =>
        <.div(
          s"ID: ${user.id}",
          s"Name: ${user.name}"
        )
      }
      .render
      .rawElement
  }

  private[this] val query = GraphqlTag.gql[Props, Vars](
    """query getUser($id: Int) {
      |  user(id: $id) { id, name }
      |}
    """.stripMargin
  )

  private[this] val schema = GraphqlTools.makeExecutableSchema(
    new ExecutableSchemaOptions(
      """type User {
        |  id: Int
        |  name: String
        |}
        |
        |type Query {
        |  user(id: Int): User
        |}
      """.stripMargin
    )
  )

  GraphqlTools.addMockFunctionsToSchema(new SchemaMockFunctionsOptions(schema))

  private[this] val client = new ApolloClient(
    new ApolloClientOptions(
      link = new ApolloMockLink(new ApolloMockLinkOptions(schema)),
      cache = new ApolloInMemoryCache(new ApolloInMemoryCacheOptions(addTypename = true)),
      ssrMode = true
    )
  )

  // scalastyle:off magic.number token
  private[this] val any = internal.ReactApollo.graphql(query.raw)(component)
  private[this] val element = React.createElement(any.asInstanceOf[ReactClassUntyped], new Vars(10))
  private[this] val root = React.createElement(
    internal.ApolloProvider.asInstanceOf[ReactClassUntyped],
    js.Dynamic.literal("client" -> client),
    element
  )

  println(ReactDOMServer.renderToString(component(new Props(new Data(new User(10, "Hello"))))))
  println(ReactDOMServer.renderToString(root))
  // scalastyle:on magic.number token

  internal.ReactApollo.renderToStringWithData(root).then[Unit] { result =>
    println(result)
  }
}
