# scalajs-graphql

[![Build Status][ci-badge]][ci]

[scalajs-graphql][site] provides GraphQL clients and utilities,
in a type-safe manner, for [Scala.js][scalajs].

*Disclaimer*: [scalajs-graphql][site] has not been released
and is under heavy development at the moment.
You could still publish locally to use it, but keep in mind that
something may not work as expected. We are working to release
the very first version soon.

## Features

* [x] GraphQL clients
  * [x] [Apollo][apollo-client]
  * [ ] [Relay][relay]
* [x] UI framework integrations
  * [x] [React][react] (with [scalajs-react][scalajs-react])
  * [ ] Maybe more
* [x] [Scala][scala] code generation (generating case classes, traits, etc.)
  * [ ] Schema
  * [x] Operations (queries, mutations, subscriptions)
  * [x] Fragments
  * [x] Input types
* [ ] Server-side rendering
* [ ] Testing utilities

## Installation

As [scalajs-graphql][site] is not released yet,
you have to build locally to use it.

```sh
$ git clone https://github.com/ngthanhtrung/scalajs-graphql.git
$ cd scalajs-graphql
$ sbt publishLocal
```

Add this to your `project/plugins.sbt` file:

```scala
addSbtPlugin("com.ngthanhtrung" % "sbt-graphql-codegen" % "0.1.0-SNAPSHOT")
```

You can then configure where [scalajs-graphql][site] puts
generated GraphQL classes in your project settings in `build.sbt`:

```scala
graphqlCodegenPackage := Some("com.example")
```

## Usage

Say you have this schema definition in `src/main/resources/schema.graphql`:

```graphql
type Name {
  firstName: String!
  lastName: String
}

enum Gender {
  MALE
  FEMALE
  UNKNOWN
}

type Person {
  name: Name!
  gender: Gender!
  age: Int
}

type Query {
  peopleByGender(gender: Gender!): [Person]
}

schema {
  query: Query
}
```

You can write your own query in
`src/main/resources/people-by-gender-query.graphql` like this:

```graphql
query PeopleByGender($gender: Gender!) {
  peopleByGender(gender: $gender) {
    name {
      firstName
      lastName
    }
    gender
    age
  }
}
```

[scalajs-graphql][site] will automatically generate
your query structure in [Scala][scala]:

```scala
package com.example

// Some parts are omitted for brevity

sealed abstract class Gender

object Gender {
  case object MALE extends Gender
  case object FEMALE extends Gender
  case object UNKNOWN extends Gender
}

// Note that "Query" suffix is added to your query name
object PeopleByGenderQuery {

  final case class Variables(gender: Gender)

  type Props = ApolloQueryProps[Data]

  final case class Data(
    peopleByGender: Option[List[Data.PeopleByGender]]
  )(raw: js.Any)

  object Data {

    final case class PeopleByGender(
      name: PeopleByGender.Name,
      gender: Gender,
      age: Option[Int]
    )(raw: js.Any)

    object PeopleByGender {

      final case class Name(
        firstName: String,
        lastName: Option[String]
      )(raw: js.Any)
    }
  }
}
```

Now you are able to write your React component in a type-safe way:

```scala
// Define your React component
val component = ScalaComponent
  .builder[PeopleByGenderQuery.Props]("PeopleByGender")
  .render_P { props =>
    props.data.personByGender.fold(
      <.div("Error occurred or data is not available yet.")
    ) { people =>
      people.toVdomArray { person =>
        <.div(
          <.div(s"First name: ${person.name.firstName}"),
          person.name.lastName.whenDefined { lastName =>
            <.div(s"Last name: $lastName")
          },
          <.div(s"Gender: ${person.gender}"),
          person.age.whenDefined { age =>
            <.div(s"Age: $age")
          }
        )
      }
    }
  }
  .build

// Declare data query for your component
val graphqlComponent = ReactApollo.graphql(PeopleByGenderQuery).apply(component)

// Create an Apollo client to talk to a GraphQL server
val apolloClient = new ApolloClient(
  link = new ApolloHttpLink(
    // Change it to your server address
    uri = "http://localhost:6789/graphql"
  ),
  cache = new ApolloInMemoryCache()
)

// Put your component inside a GraphQL environment
val apolloProvider = ApolloProvider(apolloClient)(
  graphqlComponent(PeopleByGenderQuery.Variables(Gender.FEMALE))
)

// Render everything, you would need scala-js-dom for this
apolloProvider.renderIntoDOM(dom.document.getElementById("root"))
```

## License

MIT licensed. See [LICENSE][repo-license].

[//]: # (Repository URLs)
[repo-license]: https://github.com/ngthanhtrung/scalajs-graphql/blob/master/LICENSE

[//]: # (Project URLs)
[ci]: https://travis-ci.org/ngthanhtrung/scalajs-graphql
[ci-badge]: https://travis-ci.org/ngthanhtrung/scalajs-graphql.svg
[site]: https://ngthanhtrung.github.io/scalajs-graphql/

[//]: # (External URLs)
[apollo-client]: https://www.apollographql.com/client/
[react]: https://reactjs.org/
[relay]: https://facebook.github.io/relay/
[scala]: https://www.scala-lang.org/
[scalajs]: https://www.scala-js.org/
[scalajs-react]: https://github.com/japgolly/scalajs-react
