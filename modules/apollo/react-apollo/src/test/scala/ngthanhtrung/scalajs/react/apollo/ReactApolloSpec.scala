// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.scalajs.react.apollo

import scala.scalajs.js

import japgolly.scalajs.react.raw.{React, ReactClassUntyped, ReactDOMServer}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

import ngthanhtrung.scalajs.apollo.cache.internal.{ApolloInMemoryCache, ApolloInMemoryCacheOptions}
import ngthanhtrung.scalajs.apollo.client.internal.{ApolloClient, ApolloClientOptions}
import ngthanhtrung.scalajs.apollo.graphqltools.internal.{
  ExecutableSchemaOptions,
  GraphqlTools,
  SchemaMockFunctionsOptions
}
import ngthanhtrung.scalajs.apollo.link.internal.{ApolloMockLink, ApolloMockLinkOptions}
import ngthanhtrung.scalajs.noton.{Decoder, Encoder}
import ngthanhtrung.scalajs.noton.generic.{deriveDecoder, deriveEncoder}

// scalastyle:off underscore.import
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
// scalastyle:on underscore.import

final class ReactApolloSpec extends FlatSpec with BeforeAndAfterAll {

  private[this] object GetUserQuery extends ApolloQuery {

    val raw: internal.Query[js.Object, js.Object] = internal.GraphqlTag.gql[js.Object, js.Object](
      """query GetUser($id: Int) {
        |  user(id: $id) { id, name }
        |}
      """.stripMargin
    )

    final case class Variables(id: Int)

    object Variables {
      implicit val encoder: Encoder[Variables] = deriveEncoder
    }

    final case class Data(user: Option[Data.User])(raw: js.Any)

    object Data {

      implicit val decoder: Decoder[Data] = deriveDecoder

      final case class User(id: Int, name: String)(raw: js.Any)

      object User {
        implicit val decoder: Decoder[User] = deriveDecoder
      }
    }
  }

  private[this] val component = ScalaComponent
    .builder[ApolloQueryProps[GetUserQuery.Data]]("User")
    .render_P { props =>
      props.data.user.fold(
        <.div("Loading")
      ) { user =>
        <.div(
          s"ID: ${user.id}",
          <.br(),
          s"Name: ${user.name}"
        )
      }
    }
    .build

  private[this] val graphqlComponent = ReactApollo.graphql(GetUserQuery).apply(component)

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

  private[this] val root = React.createElement(
    internal.ApolloProvider.asInstanceOf[ReactClassUntyped], // scalastyle:ignore token
    new internal.ApolloProviderProps(client),
    graphqlComponent(GetUserQuery.Variables(10)).raw // scalastyle:ignore magic.number
  )

  println(ReactDOMServer.renderToString(root))

  internal.ReactApollo.renderToStringWithData(root).`then`[Unit] { result =>
    println(result)
  }

  internal.ReactApollo.getDataFromTree(root).`then`[Unit] { _ =>
    println(ReactDOMServer.renderToStaticMarkup(root))
  }
}
