organization := "com.typesafe.sbt"
name := "sbt-uglify"
description := "sbt-web plugin for gzipping assets"
addSbtJsEngine("1.2.1")
libraryDependencies += "org.webjars.npm" % "uglify-js" % "2.8.14"