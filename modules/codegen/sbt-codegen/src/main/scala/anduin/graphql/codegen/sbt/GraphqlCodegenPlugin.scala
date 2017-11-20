// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen.sbt

// scalastyle:off underscore.import
import sbt._
import sbt.Keys._
// scalastyle:on underscore.import

object GraphqlCodegenPlugin extends AutoPlugin {

  override lazy val trigger: PluginTrigger = noTrigger
  override lazy val requires: Plugins = plugins.JvmPlugin

  object autoImport {

    val GraphqlCodegen = config("graphql-codegen").hide

    val graphqlCodegenSchema = TaskKey[File](
      "graphql-codegen-schema",
      "GraphQL schema file"
    )

    val graphqlCodegenDocuments = TaskKey[Seq[File]](
      "graphql-codegen-documents",
      "GraphQL document files"
    )

    val graphqlCodegenPackage = SettingKey[Option[String]](
      "graphql-codegen-package",
      "Package of GraphQL generated code"
    )

    val graphqlCodegenCleanBeforehand = SettingKey[Boolean](
      "graphql-codegen-clean-beforehand",
      "Whether to clean output directory before generating code."
    )

    val graphqlCodegen = TaskKey[File](
      "graphql-codegen",
      "Generate GraphQL code"
    )
  }

  import autoImport._ // scalastyle:ignore import.grouping underscore.import

  private lazy val projectDefaultSettings: Seq[Def.Setting[_]] =
    Seq(
      ivyConfigurations += GraphqlCodegen,
      libraryDependencies ++= List(
        "com.anduintransact" %% "graphql-codegen-cli" % BuildInfo.version % GraphqlCodegen,
        "com.anduintransact" %% "scalajs-noton-generic" % BuildInfo.version
      ),
      mainClass in GraphqlCodegen := Some("anduin.graphql.codegen.cli.CodegenCli"),
      fullClasspath in GraphqlCodegen := Classpaths
        .managedJars(GraphqlCodegen, classpathTypes.value, update.value),
      runner in run in GraphqlCodegen := {
        val forkOptions = ForkOptions(
          javaHome = javaHome.value,
          outputStrategy = outputStrategy.value,
          bootJars = Nil,
          workingDirectory = Some(baseDirectory.value),
          runJVMOptions = javaOptions.value,
          connectInput = connectInput.value,
          envVars = envVars.value
        )
        new ForkRun(forkOptions)
      }
    )

  def projectScopedSettings(config: Configuration): Seq[Def.Setting[_]] = {
    inConfig(config)(
      Seq(
        graphqlCodegenSchema := resourceDirectory.value / "schema.graphql",
        resourceDirectories in graphqlCodegen := resourceDirectories.value,
        includeFilter in graphqlCodegen := "*.graphql",
        excludeFilter in graphqlCodegen := HiddenFileFilter,
        graphqlCodegenDocuments := Defaults
          .collectFiles(
            resourceDirectories in graphqlCodegen,
            includeFilter in graphqlCodegen,
            excludeFilter in graphqlCodegen
          )
          .value,
        graphqlCodegenPackage := None,
        graphqlCodegenCleanBeforehand := true,
        graphqlCodegen := {
          val output = sourceManaged.value / "sbt-graphql-codegen"

          if (graphqlCodegenCleanBeforehand.value) {
            IO.delete(output)
          }

          val documents = graphqlCodegenDocuments.value.map(_.getAbsolutePath)
          val packageOption = graphqlCodegenPackage.value.toSeq.flatMap { `package` =>
            Seq("--package", `package`)
          }

          val options = Seq("gen-ops", "--schema", graphqlCodegenSchema.value.getAbsolutePath) ++
            packageOption ++
            Seq("--output", output.getAbsolutePath) ++
            documents

          (runner in run in GraphqlCodegen).value.run(
            (mainClass in GraphqlCodegen).value.get,
            Attributed.data((fullClasspath in GraphqlCodegen).value),
            options,
            streams.value.log
          )

          output
        },
        sourceGenerators += Def.task { Seq(graphqlCodegen.value) }
      )
    )
  }

  override lazy val projectSettings: Seq[Def.Setting[_]] =
    projectDefaultSettings ++
      projectScopedSettings(Compile)
}
