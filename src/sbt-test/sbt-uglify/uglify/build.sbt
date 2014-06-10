lazy val root = (project in file(".")).enablePlugins(SbtWeb)

pipelineStages := Seq(uglify)

val checkMapFileContents = taskKey[Unit]("check that map contents are correct")

checkMapFileContents := {
  val contents = IO.read(file("target/web/stage/javascripts/main.min.js.map"))
  if (contents != """{"version":3,"file":"main.min.js","sources":["a.js","b.js","x.js"],"names":["a","b","define","number","opposite","call","this"],"mappings":"AAAA,QAASA,KACR,MAAO,GCDR,QAASC,KACR,MAAO,ICDR,WACEC,OAAO,WACL,GAAIC,GAAQC,CAEZ,OADAD,GAAS,GACFC,GAAW,MAGnBC,KAAKC"}""") {
    sys.error(s"Unexpected contents: $contents")
  }
}