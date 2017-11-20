// Copyright (C) 2017 Anduin Transactions, Inc.

lazy val `scalajs-noton-generic` = LocalProject("scalajs-noton-generic")

lazy val `graphql-codegen` = project
  .in(file("codegen"))
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

lazy val `graphql-codegen-cli` = project
  .in(file("codegen-cli"))
  .dependsOn(`graphql-codegen`)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.CaseApp.value,
      Dependencies.CirceParser.value,
      Dependencies.Http4sClient.value,
      Dependencies.SangriaCirce.value,
      Dependencies.Slf4jNop.value
    ),
    fork in run := true
  )

lazy val `sbt-graphql-codegen` = project
  .in(file("sbt-codegen"))
  .settings(
    sbtPlugin := true,
    scalaVersion := "2.10.7",
    buildInfoKeys := Seq[BuildInfoKey](version),
    buildInfoPackage := "anduin.graphql.codegen.sbt",
    publishLocal := publishLocal
      .dependsOn(publishLocal in `graphql-codegen-cli`)
      .dependsOn(publishLocal in `scalajs-noton-generic`)
  )
  .enablePlugins(BuildInfoPlugin)

lazy val codegen = project
  .in(file("."))
  .aggregate(
    `graphql-codegen`,
    `graphql-codegen-cli`,
    `sbt-graphql-codegen`
  )
