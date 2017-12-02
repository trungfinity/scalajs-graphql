// Copyright (C) 2017 Anduin Transactions, Inc.

// scalastyle:off underscore.import
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt._
import sbt.Keys._
// scalastyle:on underscore.import

object CommonSettingPlugin extends AutoPlugin {

  override lazy val trigger: PluginTrigger = allRequirements
  override lazy val requires: Plugins = plugins.JvmPlugin

  object autoImport extends DependencyImports

  import autoImport._ // scalastyle:ignore import.grouping underscore.import

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      Dependencies.ScalaTest.value
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature"
    ),
    scalacOptions ++= {
      if (isScalaJSProject.value) {
        Seq("-P:scalajs:sjsDefinedByDefault")
      } else {
        Seq.empty
      }
    },
    parallelExecution in Test := false
  )

  override lazy val buildSettings: Seq[Def.Setting[_]] = Seq(
    organization := "com.ngthanhtrung",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.12.4",
    scalafmtShowDiff in scalafmt := true
  )
}
