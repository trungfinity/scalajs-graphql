// Copyright (C) 2017 Anduin Transactions, Inc.

// scalastyle:off multiple.string.literals

lazy val `scalajs-io` = project
  .in(file("modules") / "io")
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.CatsEffect.value
    )
  )
  .enablePlugins(ScalaJSPlugin)

lazy val `graphql-codegen` = project
  .in(file("modules") / "graphql-codegen")
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.CatsCore.value,
      Dependencies.Sangria.value,
      Dependencies.Scalameta.value
    ),
    scalacOptions ++= Seq(
      "-Ypartial-unification"
    )
  )

lazy val apollo = project in (file("modules") / "apollo")
lazy val fetch = project in (file("modules") / "fetch")
lazy val noton = project in (file("modules") / "noton")

lazy val `scala-graphql` = project
  .in(file("."))
  .aggregate(fetch, apollo)
  .aggregate(
    `scalajs-io`,
    `graphql-codegen`
  )
