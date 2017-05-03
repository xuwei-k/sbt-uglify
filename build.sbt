sbtPlugin := true

organization := "com.typesafe.sbt"

name := "sbt-uglify"

version := "1.0.4-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "org.webjars.npm" % "uglify-js" % "2.7.3"
)

resolvers ++= Seq(
  "Typesafe Releases Repository" at "https://repo.typesafe.com/typesafe/releases/"
)

addSbtPlugin("com.typesafe.sbt" % "sbt-js-engine" % "1.2.0")

publishMavenStyle := false

publishTo := {
  if (isSnapshot.value) Some(Classpaths.sbtPluginSnapshots)
  else Some(Classpaths.sbtPluginReleases)
}

scriptedSettings

scriptedLaunchOpts <+= version apply { v => s"-Dproject.version=$v" }
