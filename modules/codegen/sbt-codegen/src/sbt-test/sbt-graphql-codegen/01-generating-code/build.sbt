// Copyright (C) 2017 Anduin Transactions, Inc.

lazy val root = project
  .in(file("."))
  .settings(
    scalaVersion := "2.12.4"
  )
  .enablePlugins(GraphqlCodegenPlugin)
