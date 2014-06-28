lazy val root = (project in file(".")).enablePlugins(SbtWeb)

libraryDependencies += "org.webjars" % "bootstrap" % "3.0.2"

pipelineStages := Seq(uglify)

UglifyKeys.uglifyOps := { js =>
  Seq((js.sortBy(_._2), "concat.min.js"))
}

val checkMapFileContents = taskKey[Unit]("check that map contents are correct")

checkMapFileContents := {
  val contents = IO.read(file("target/web/stage/concat.min.js.map"))
  if (!contents.contains("""{"version":3,"file":"concat.min.js","sources":["javascripts/a.js","javascripts/b.js","javascripts/x.js"],"names":["a","b","define","number","opposite","call","this"],"mappings":"""))
    sys.error(s"Unexpected contents: $contents")
}
