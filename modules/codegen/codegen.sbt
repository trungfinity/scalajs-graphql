// Copyright (C) 2017 Anduin Transactions, Inc.

lazy val `scalajs-noton-core` = LocalProject("scalajs-noton-core")
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
    libraryDependencies ++= Seq(
      Defaults.sbtPluginExtra(
        Dependencies.Scalajs.value,
        (sbtBinaryVersion in update).value,
        (scalaBinaryVersion in update).value
      )
    ),
    buildInfoKeys := Seq[BuildInfoKey](version),
    buildInfoPackage := "anduin.graphql.codegen.sbt",
    publishLocal := publishLocal
      .dependsOn(
        // Hacky, why does this project have to know all transitive dependencies?
        publishLocal in `graphql-codegen`,
        publishLocal in `graphql-codegen-cli`,
        publishLocal in `scalajs-noton-core`,
        publishLocal in `scalajs-noton-generic`
      )
      .value,
    scriptedSettings,
    scriptedLaunchOpts ++= Seq(
      "-Xmx1024M",
      "-Dplugin.version=" + version.value
    ),
    scriptedBufferLog := false
  )
  .enablePlugins(BuildInfoPlugin)

lazy val codegen = project
  .in(file("."))
  .aggregate(
    `graphql-codegen`,
    `graphql-codegen-cli`,
    `sbt-graphql-codegen`
  )
