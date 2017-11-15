// Copyright (C) 2017 Anduin Transactions, Inc.

// scalastyle:off multiple.string.literals

lazy val `scalajs-fetch` = LocalProject("scalajs-fetch")
lazy val `scalajs-node-fetch` = LocalProject("scalajs-node-fetch")

lazy val `scalajs-graphql-tools` = project
  .in(file("graphql-tools"))
  .settings(
    npmDependencies in Compile ++= Seq(
      "graphql" -> "0.11.7",
      "graphql-tools" -> "2.7.2"
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-apollo-link` = project
  .in(file("apollo-link"))
  .settings(
    npmDependencies in Compile ++= Seq(
      "apollo-link" -> "1.0.0",
      "graphql" -> "0.11.7"
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
      "apollo-link-http" -> "1.1.0"
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
      "apollo-cache" -> "1.0.0"
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-apollo-cache-inmemory` = project
  .in(file("apollo-cache-inmemory"))
  .dependsOn(`scalajs-apollo-cache`)
  .settings(
    npmDependencies in Compile ++= Seq(
      "apollo-cache-inmemory" -> "1.0.0"
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
      "apollo-client" -> "2.0.1"
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-react-apollo` = project
  .in(file("react-apollo"))
  .dependsOn(
    `scalajs-apollo-client`,
    `scalajs-graphql-tools` % Test,
    `scalajs-apollo-link-mock` % Test,
    `scalajs-apollo-cache-inmemory` % Test
  )
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
      "io.circe" %%% "circe-generic" % "0.9.0-M2",
      "io.circe" %%% "circe-scalajs" % "0.9.0-M2",
      "com.github.japgolly.scalajs-react" %%% "core" % "1.1.1"
    ),
    npmDependencies in Compile ++= Seq(
      "graphql-tag" -> "2.5.0",
      "react" -> "15.6.2",
      "react-apollo" -> "2.0.0",
      "react-dom" -> "15.6.2"
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
