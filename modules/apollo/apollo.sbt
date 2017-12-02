// Copyright (C) 2017 Anduin Transactions, Inc.

// scalastyle:off multiple.string.literals

lazy val `scalajs-fetch` = LocalProject("scalajs-fetch")
lazy val `scalajs-node-fetch` = LocalProject("scalajs-node-fetch")

lazy val `scalajs-noton-generic` = LocalProject("scalajs-noton-generic")

lazy val `scalajs-graphql-tools` = project
  .in(file("graphql-tools"))
  .settings(
    npmDependencies in Compile ++= Seq(
      NpmDependencies.Graphql,
      NpmDependencies.GraphqlTools
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-apollo-link` = project
  .in(file("apollo-link"))
  .settings(
    npmDependencies in Compile ++= Seq(
      NpmDependencies.ApolloLink,
      NpmDependencies.Graphql
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-apollo-link-http` = project
  .in(file("apollo-link-http"))
  .dependsOn(
    `scalajs-apollo-link`,
    `scalajs-fetch`,
    `scalajs-node-fetch` % Test
  )
  .settings(
    npmDependencies in Compile ++= Seq(
      NpmDependencies.ApolloLinkHttp
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-apollo-link-mock` = project
  .in(file("apollo-link-mock"))
  .dependsOn(`scalajs-apollo-link`)
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-apollo-cache` = project
  .in(file("apollo-cache"))
  .settings(
    npmDependencies in Compile ++= Seq(
      NpmDependencies.ApolloCache
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-apollo-cache-inmemory` = project
  .in(file("apollo-cache-inmemory"))
  .dependsOn(`scalajs-apollo-cache`)
  .settings(
    npmDependencies in Compile ++= Seq(
      NpmDependencies.ApolloCacheInmemory
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-apollo-client` = project
  .in(file("apollo-client"))
  .dependsOn(
    `scalajs-apollo-link`,
    `scalajs-apollo-cache`,
    `scalajs-node-fetch` % Test,
    `scalajs-apollo-link-http` % Test,
    `scalajs-apollo-cache-inmemory` % Test
  )
  .settings(
    npmDependencies in Compile ++= Seq(
      NpmDependencies.ApolloClient
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-react-apollo` = project
  .in(file("react-apollo"))
  .dependsOn(
    `scalajs-apollo-client`,
    `scalajs-noton-generic`,
    `scalajs-graphql-tools` % Test,
    `scalajs-apollo-link-mock` % Test,
    `scalajs-apollo-cache-inmemory` % Test
  )
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin(Dependencies.ScalaMacrosParadise.value),
      Dependencies.CirceGeneric.value,
      Dependencies.CirceScalajs.value,
      Dependencies.ScalajsReactCore.value
    ),
    npmDependencies in Compile ++= Seq(
      NpmDependencies.GraphqlTag,
      NpmDependencies.React,
      NpmDependencies.ReactApollo,
      NpmDependencies.ReactDom
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val apollo = project
  .in(file("."))
  .aggregate(
    `scalajs-graphql-tools`,
    `scalajs-apollo-link`,
    `scalajs-apollo-link-http`,
    `scalajs-apollo-link-mock`,
    `scalajs-apollo-cache`,
    `scalajs-apollo-cache-inmemory`,
    `scalajs-apollo-client`,
    `scalajs-react-apollo`
  )
