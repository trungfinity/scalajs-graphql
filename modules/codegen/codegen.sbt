// scalastyle:off multiple.string.literals

lazy val `scalajs-apollo-cache` = LocalProject("scalajs-apollo-cache")
lazy val `scalajs-apollo-cache-inmemory` = LocalProject("scalajs-apollo-cache-inmemory")
lazy val `scalajs-apollo-client` = LocalProject("scalajs-apollo-client")
lazy val `scalajs-apollo-link` = LocalProject("scalajs-apollo-link")
lazy val `scalajs-apollo-link-http` = LocalProject("scalajs-apollo-link-http")
lazy val `scalajs-apollo-link-mock` = LocalProject("scalajs-apollo-link-mock")
lazy val `scalajs-graphql-tools` = LocalProject("scalajs-graphql-tools")
lazy val `scalajs-react-apollo` = LocalProject("scalajs-react-apollo")

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
        (sbtBinaryVersion in pluginCrossBuild).value,
        (scalaBinaryVersion in pluginCrossBuild).value
      )
    ),
    buildInfoKeys := Seq[BuildInfoKey](version),
    buildInfoPackage := "ngthanhtrung.graphql.codegen.sbt",
    test := (),
    publishLocal := publishLocal
      .dependsOn(
        // Hacky, why does this project have to know all transitive dependencies?
        publishLocal in `graphql-codegen`,
        publishLocal in `graphql-codegen-cli`,
        publishLocal in `scalajs-apollo-cache`,
        publishLocal in `scalajs-apollo-cache-inmemory`,
        publishLocal in `scalajs-apollo-client`,
        publishLocal in `scalajs-apollo-link`,
        publishLocal in `scalajs-apollo-link-http`,
        publishLocal in `scalajs-apollo-link-mock`,
        publishLocal in `scalajs-graphql-tools`,
        publishLocal in `scalajs-react-apollo`,
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
