// Copyright (C) 2017 Anduin Transactions, Inc.

lazy val `scalajs-io` = LocalProject("scalajs-io")

lazy val `scalajs-fetch` = project
  .in(file("fetch"))
  .dependsOn(`scalajs-io`)
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-node-fetch` = project
  .in(file("node-fetch"))
  .dependsOn(`scalajs-fetch`)
  .settings(
    npmDependencies in Compile ++= Seq(
      NpmDependencies.NodeFetch
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val fetch = project
  .in(file("."))
  .aggregate(
    `scalajs-fetch`,
    `scalajs-node-fetch`
  )
