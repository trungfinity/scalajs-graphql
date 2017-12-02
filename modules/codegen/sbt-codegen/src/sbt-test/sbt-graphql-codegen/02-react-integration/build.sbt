// Copyright (C) 2017 Anduin Transactions, Inc.

lazy val libraryVersion = System.getProperty("plugin.version")

lazy val root = project
  .in(file("."))
  .settings(
    scalaVersion := "2.12.4",
    libraryDependencies ++= {
      if (libraryVersion != null) { // scalastyle:ignore null
        Seq(
          // scalastyle:off multiple.string.literals
          "com.anduintransact" %%% "scalajs-graphql-tools" % libraryVersion,
          "com.anduintransact" %%% "scalajs-apollo-link-mock" % libraryVersion,
          "com.anduintransact" %%% "scalajs-apollo-cache-inmemory" % libraryVersion
          // scalastyle:on multiple.string.literals
        )
      } else {
        throw new RuntimeException("Library version is not specified.")
      }
    },
    graphqlCodegenPackage in Compile := Some("anduin.graphql.example"),
    scalaJSUseMainModuleInitializer in Compile := true
  )
  .enablePlugins(GraphqlCodegenPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
