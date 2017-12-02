lazy val pluginVersion = System.getProperty("plugin.version")

{
  if (pluginVersion != null) { // scalastyle:ignore null
    addSbtPlugin("com.ngthanhtrung" % "sbt-graphql-codegen" % pluginVersion)
  } else {
    throw new RuntimeException("Plugin version is not specified.")
  }
}
