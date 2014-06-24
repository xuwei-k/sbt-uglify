lazy val root = (project in file(".")).enablePlugins(SbtWeb)

libraryDependencies += "org.webjars" % "bootstrap" % "3.0.2"

pipelineStages := Seq(uglify)

val checkMapFileContents = taskKey[Unit]("check that map contents are correct")

checkMapFileContents := {
  val contents = IO.read(file("target/web/stage/javascripts/a.min.js.map"))
  val r = """\{"version":3,"file":"a.min.js","sources":\["a.js"\],"names":\["a"\],"mappings":"AAAA,QAASA,KACR,MAAO"\}""".r
  if (r.findAllIn(contents).isEmpty) {
    sys.error(s"Unexpected contents: $contents")
  }
}