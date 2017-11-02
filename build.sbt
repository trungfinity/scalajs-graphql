scalaVersion in ThisBuild := "2.12.4"

lazy val `scalajs-io` = project
  .in(file("modules") / "io")
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.0.4" % Test,
      "org.typelevel" %%% "cats-effect" % "0.5"
    )
  )
  .enablePlugins(ScalaJSPlugin)

lazy val `scalajs-fetch` = project
  .in(file("modules") / "fetch")
  .dependsOn(`scalajs-io`)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.0.4" % Test
    ),

    scalacOptions ++= Seq(
      "-P:scalajs:sjsDefinedByDefault"
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-node-fetch` = project
  .in(file("modules") / "node-fetch")
  .dependsOn(`scalajs-fetch`)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.0.4" % Test
    ),

    scalacOptions ++= Seq(
      "-P:scalajs:sjsDefinedByDefault"
    ),

    npmDependencies in Compile ++= Seq(
      "node-fetch" -> "1.7.3"
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val `scalajs-apollo` = project
  .in(file("modules") / "apollo")
  .dependsOn(
    `scalajs-fetch`,
    `scalajs-node-fetch` % Test
  )
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
      "org.scalatest" %%% "scalatest" % "3.0.4" % Test,
      "io.circe" %%% "circe-generic" % "0.9.0-M2",
      "io.circe" %%% "circe-scalajs" % "0.9.0-M2",
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
      "react" -> "15.6.2",
      "react-apollo" -> "2.0.0",
      "react-dom" -> "15.6.2"
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)
