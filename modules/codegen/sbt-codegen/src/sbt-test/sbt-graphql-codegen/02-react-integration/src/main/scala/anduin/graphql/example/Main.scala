// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.example

import scala.scalajs.js

import japgolly.scalajs.react.raw.{React, ReactClassUntyped, ReactDOMServer}

import anduin.scalajs.apollo.cache.internal.{ApolloInMemoryCache, ApolloInMemoryCacheOptions}
import anduin.scalajs.apollo.client.internal.{ApolloClient, ApolloClientOptions}
import anduin.scalajs.apollo.graphqltools.internal.{
  ExecutableSchemaOptions,
  GraphqlTools,
  SchemaMockFunctionsOptions
}
import anduin.scalajs.apollo.link.internal.{ApolloMockLink, ApolloMockLinkOptions}
import anduin.scalajs.react.apollo.{ApolloQueryProps, ReactApollo}
import anduin.scalajs.react.apollo.internal.{
  ApolloProvider => InternalApolloProvider,
  ApolloProviderProps => InternalApolloProviderProps,
  ReactApollo => InternalReactApollo
}

// scalastyle:off underscore.import
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
// scalastyle:on underscore.import

object Main {

  def testPersonByNameQuery(): Unit = {
    val component = ScalaComponent
      .builder[ApolloQueryProps[GetPersonByNameQuery.Data]]("PersonByName")
      .render_P { props =>
        props.data.personByName.fold(
          <.div("Loading")
        ) { person =>
          <.div(
            person.name.whenDefined { name =>
              <.div(
                s"First name: ${name.firstName.value}",
                <.br(),
                name.lastName.whenDefined { lastName =>
                  s"Last name: $lastName",
                }
              )
            },
            person.age.whenDefined { age =>
              s"Age: $age"
            }
          )
        }
      }
      .build

    val graphqlComponent = ReactApollo.graphql(GetPersonByNameQuery).apply(component)

    val schema = GraphqlTools.makeExecutableSchema(
      new ExecutableSchemaOptions(
        // Fix it later
        """type FirstName {
          |  value: String!
          |}
          |
          |type Name {
          |  firstName: FirstName!
          |  lastName: String
          |}
          |
          |input FirstNameInput {
          |  value: String!
          |}
          |
          |input NameInput {
          |  firstName: FirstNameInput!
          |  lastName: String
          |}
          |
          |enum Gender {
          |  MALE
          |  FEMALE
          |  UNKNOWN
          |}
          |
          |type Person {
          |  name: Name
          |  gender: Gender
          |  age: Int
          |}
          |
          |type Query {
          |  personByName(name: NameInput!): Person
          |  peopleByGender(gender: Gender!): [Person]
          |}
          |
          |schema {
          |  query: Query
          |}
        """.stripMargin
      )
    )

    GraphqlTools.addMockFunctionsToSchema(new SchemaMockFunctionsOptions(schema))

    val client = new ApolloClient(
      new ApolloClientOptions(
        link = new ApolloMockLink(new ApolloMockLinkOptions(schema)),
        cache = new ApolloInMemoryCache(new ApolloInMemoryCacheOptions(addTypename = true)),
        ssrMode = true
      )
    )

    val firstNameInput = FirstNameInput("Trung")
    val nameInput = NameInput(firstNameInput, Some("Nguyen"))

    val root = React.createElement(
      InternalApolloProvider.asInstanceOf[ReactClassUntyped], // scalastyle:ignore token
      new InternalApolloProviderProps(client),
      graphqlComponent(GetPersonByNameQuery.Variables(nameInput)).raw // scalastyle:ignore magic.number
    )

    InternalReactApollo.getDataFromTree(root).`then`[Unit] { _ =>
      val markup = ReactDOMServer.renderToStaticMarkup(root)
      assert(markup.contains("<div>First name: Hello World<br/>Last name: Hello World</div>"))
    }
  }

  def main(args: Array[String]): Unit = {
    testPersonByNameQuery()
  }
}
