// Copyright (C) 2017 Anduin Transactions, Inc.

// scalastyle:off multiple.string.literals

inThisBuild(
  Seq(
    scalaVersion := "2.12.4"
  )
)

lazy val `scalajs-io` = project
  .in(file("modules") / "io")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % "0.5"
    )
  )
  .enablePlugins(ScalaJSPlugin)

lazy val `graphql-codegen` = project
  .in(file("modules") / "graphql-codegen")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "1.0.0-RC1",
      "org.sangria-graphql" %% "sangria" % "1.3.2",
      "org.scalameta" %% "scalameta" % "2.1.2"
    ),
    scalacOptions ++= Seq(
      "-Ypartial-unification"
    )
  )

lazy val fetch = project in (file("modules") / "fetch")
lazy val apollo = project in (file("modules") / "apollo")

lazy val `scala-graphql` = project
  .in(file("."))
  .aggregate(fetch, apollo)
  .aggregate(
    `scalajs-io`,
    `graphql-codegen`
  )
