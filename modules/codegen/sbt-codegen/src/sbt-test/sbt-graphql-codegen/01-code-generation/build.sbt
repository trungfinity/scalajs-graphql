// Copyright (C) 2017 Anduin Transactions, Inc.

lazy val root = project
  .in(file("."))
  .settings(
    scalaVersion := "2.12.4",
    graphqlCodegenPackage in Compile := Some("ngthanhtrung.graphql.example"),
    scalaJSUseMainModuleInitializer in Compile := true
  )
  .enablePlugins(GraphqlCodegenPlugin)
