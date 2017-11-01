scalaVersion in ThisBuild := "2.12.4"

lazy val `scalajs-react-apollo` = project
  .in(file("modules") / "react-apollo")
  .settings()
  .enablePlugins(ScalaJSBundlerPlugin)
