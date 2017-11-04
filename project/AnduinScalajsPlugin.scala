import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._

object AnduinScalajsPlugin extends AutoPlugin {

  override lazy val trigger: PluginTrigger = allRequirements
  override lazy val requires: Plugins = ScalaJSPlugin

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    scalaVersion := "2.12.4",

    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.0.4" % Test
    ),

    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-P:scalajs:sjsDefinedByDefault"
    )
  )

  override lazy val buildSettings: Seq[Def.Setting[_]] = Seq(
    scalafmtShowDiff in scalafmt := true
  )
}
