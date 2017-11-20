// Copyright (C) 2017 Anduin Transactions, Inc.

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
    sbtPlugin := true
  )

lazy val codegen = project
  .in(file("."))
  .aggregate(
    `graphql-codegen`,
    `graphql-codegen-cli`,
    `sbt-graphql-codegen`
  )
