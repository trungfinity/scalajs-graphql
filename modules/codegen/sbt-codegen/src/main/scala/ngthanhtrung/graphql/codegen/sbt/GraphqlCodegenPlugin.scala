// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.graphql.codegen.sbt

import org.scalajs.sbtplugin.ScalaJSPlugin

// scalastyle:off underscore.import
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt._
import sbt.Keys._
// scalastyle:on underscore.import

// scalastyle:off multiple.string.literals

object GraphqlCodegenPlugin extends AutoPlugin {

  override lazy val trigger: PluginTrigger = noTrigger
  override lazy val requires: Plugins = ScalaJSPlugin

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

    val graphqlCodegen = TaskKey[Seq[File]](
      "graphql-codegen",
      "Generate GraphQL code"
    )
  }

  import autoImport._ // scalastyle:ignore import.grouping underscore.import

  private lazy val projectDefaultSettings: Seq[Def.Setting[_]] =
    Seq(
      ivyConfigurations += GraphqlCodegen,
      libraryDependencies ++= List(
        "com.ngthanhtrung" %% "graphql-codegen-cli" % BuildInfo.version % GraphqlCodegen,
        "com.ngthanhtrung" %%% "scalajs-react-apollo" % BuildInfo.version,
        "com.ngthanhtrung" %%% "scalajs-noton-generic" % BuildInfo.version
      ),
      mainClass in GraphqlCodegen := Some("ngthanhtrung.graphql.codegen.cli.CodegenCli"),
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

          val schema = graphqlCodegenSchema.value.getAbsolutePath
          val documents = graphqlCodegenDocuments.value
            .map(_.getAbsolutePath)
            .filter(_ != schema)

          val packageOption = graphqlCodegenPackage.value.toSeq.flatMap { `package` =>
            Seq("--package", `package`)
          }

          val options = Seq("gen-ops", "--schema", schema) ++
            packageOption ++
            Seq("--output", output.getAbsolutePath) ++
            documents

          (runner in run in GraphqlCodegen).value.run(
            (mainClass in GraphqlCodegen).value.get,
            Attributed.data((fullClasspath in GraphqlCodegen).value),
            options,
            streams.value.log
          )

          (output ** "*.scala").get
        },
        sourceGenerators += graphqlCodegen.taskValue
      )
    )
  }

  override lazy val projectSettings: Seq[Def.Setting[_]] =
    projectDefaultSettings ++
      projectScopedSettings(Compile)
}
