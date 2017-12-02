// scalastyle:off multiple.string.literals

lazy val `scalajs-io` = project
  .in(file("modules") / "io")
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.CatsEffect.value
    )
  )
  .enablePlugins(ScalaJSPlugin)

lazy val apollo = project in (file("modules") / "apollo")
lazy val codegen = project in (file("modules") / "codegen")
lazy val fetch = project in (file("modules") / "fetch")
lazy val noton = project in (file("modules") / "noton")

lazy val `scala-graphql` = project
  .in(file("."))
  .aggregate(
    apollo,
    codegen,
    fetch,
    noton
  )
  .aggregate(
    `scalajs-io`
  )
