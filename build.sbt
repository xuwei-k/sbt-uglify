sbtPlugin := true

organization := "com.typesafe.sbt"

name := "sbt-uglify"

version := "1.0.5-SNAPSHOT"

scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "org.webjars.npm" % "graceful-readlink" % "1.0.1",
  "org.webjars.npm" % "uglify-js" % "2.8.14",
  "io.monix" %% "monix" % "2.3.0"
)

resolvers ++= Seq(
  "Typesafe Releases Repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.url("sbt snapshot plugins", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns),
  Resolver.sonatypeRepo("snapshots"),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  Resolver.mavenLocal
)

addCrossSbtPlugin("com.typesafe.sbt" % "sbt-js-engine" % "1.2.2")
addCrossSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.2")

def addCrossSbtPlugin(dependency: ModuleID): Setting[Seq[ModuleID]] =
  libraryDependencies += {
    val sbtV = (sbtBinaryVersion in pluginCrossBuild).value
    val scalaV = (scalaBinaryVersion in update).value
    Defaults.sbtPluginExtra(dependency, sbtV, scalaV)
  }

publishMavenStyle := false

publishTo := {
  if (isSnapshot.value) Some(Classpaths.sbtPluginSnapshots)
  else Some(Classpaths.sbtPluginReleases)
}

crossSbtVersions := Seq("0.13.16", "1.0.0")

ScriptedPlugin.scriptedSettings
scriptedLaunchOpts += s"-Dproject.version=${version.value}"
scriptedBufferLog := false
