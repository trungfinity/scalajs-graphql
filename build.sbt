scalaVersion in ThisBuild := "2.12.4"

lazy val `scalajs-io` = project
  .in(file("modules") / "io")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % "0.5"
    )
  )
  .enablePlugins(ScalaJSPlugin)

lazy val `scalajs-fetch` = project
  .in(file("modules") / "fetch")
  .dependsOn(`scalajs-io`)
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-node-fetch` = project
  .in(file("modules") / "node-fetch")
  .dependsOn(`scalajs-fetch`)
  .settings(
    npmDependencies in Compile ++= Seq(
      "node-fetch" -> "1.7.3"
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-apollo-link` = project
  .in(file("modules") / "apollo-link")
  .settings(
    npmDependencies in Compile ++= Seq(
      "apollo-link" -> "1.0.0"
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-apollo-link-http` = project
  .in(file("modules") / "apollo-link-http")
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

lazy val `scalajs-apollo-cache` = project
  .in(file("modules") / "apollo-cache")
  .settings(
    npmDependencies in Compile ++= Seq(
      "apollo-cache" -> "1.0.0"
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-apollo-cache-inmemory` = project
  .in(file("modules") / "apollo-cache-inmemory")
  .dependsOn(`scalajs-apollo-cache`)
  .settings(
    npmDependencies in Compile ++= Seq(
      "apollo-cache-inmemory" -> "1.0.0"
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-apollo-client` = project
  .in(file("modules") / "apollo-client")
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
  .in(file("modules") / "react-apollo")
  .dependsOn(
    `scalajs-apollo-client`,
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
      "apollo-link-core" -> "0.5.4",
      "graphql" -> "0.11.7",
      "graphql-tag" -> "2.5.0",
      "graphql-tools" -> "2.7.2",
      "react" -> "15.6.2",
      "react-apollo" -> "2.0.0",
      "react-dom" -> "15.6.2"
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)
