scalaVersion in ThisBuild := "2.12.4"

lazy val `scalajs-apollo` = project
  .in(file("modules") / "apollo")
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
      "org.scalatest" %%% "scalatest" % "3.0.4" % Test,
      "io.circe" %%% "circe-generic" % "0.9.0-M1",
      "io.circe" %%% "circe-scalajs" % "0.9.0-M1",
      "com.github.japgolly.scalajs-react" %%% "core" % "1.1.1"
    ),

    scalacOptions ++= Seq(
      "-P:scalajs:sjsDefinedByDefault"
    ),

    npmDependencies in Test ++= Seq(
      "apollo-cache" -> "1.0.0",
      "apollo-cache-inmemory" -> "1.0.0",
      "apollo-client" -> "2.0.1",
      "apollo-link" -> "1.0.0",
      "apollo-link-http" -> "1.1.0",
      "graphql" -> "0.11.7",
      "node-fetch" -> "1.7.3",
      "react" -> "15.6.2",
      "react-apollo" -> "2.0.0",
      "react-dom" -> "15.6.2"
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)
