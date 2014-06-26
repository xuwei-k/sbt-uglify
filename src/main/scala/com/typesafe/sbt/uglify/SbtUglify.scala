package com.typesafe.sbt.uglify

import sbt._
import sbt.Keys._
import com.typesafe.sbt.web.{incremental, SbtWeb}
import com.typesafe.sbt.web.pipeline.Pipeline
import com.typesafe.sbt.jse.{SbtJsEngine, SbtJsTask}
import com.typesafe.sbt.web.incremental._
import sbt.Task
import com.typesafe.sbt.web.incremental.OpSuccess

object Import {

  val uglify = TaskKey[Pipeline.Stage]("uglify", "Perform Uglify optimization on the asset pipeline.")

  object UglifyKeys {
    val appDir = SettingKey[File]("uglify-build-dir", "Where uglifyjs will read from. It likes to have all the files in one place.")
    val buildDir = SettingKey[File]("uglify-build-dir", "Where uglifyjs will write to.")
    val comments = SettingKey[Option[String]]("uglify-comments", "Specifies comments handling. Defaults to None.")
    val compress = SettingKey[Boolean]("uglify-compress", "Enables compression. The default is to compress.")
    val compressOptions = SettingKey[Seq[String]]("uglify-compress-options", "Options for compression such as hoist_vars, if_return etc.")
    val define = SettingKey[Option[String]]("uglify-define", "Define globals. Defaults to None.")
    val enclose = SettingKey[Boolean]("uglify-enclose", "Enclose in one big function. Defaults to false.")
    val mangle = SettingKey[Boolean]("uglify-mangle", "Enables name mangling. The default is to mangle.")
    val mangleOptions = SettingKey[Seq[String]]("uglify-mangle-options", "Options for mangling such as sort, topLevel etc.")
    val preamble = SettingKey[Option[String]]("uglify-preamble", "Any preamble to include at the start of the output. Defaults to None")
    val reserved = SettingKey[Seq[String]]("uglify-reserved", "Reserved names to exclude from mangling.")
    val sourceMap = SettingKey[Boolean]("uglify-source-map", "Enables source maps. The default is that source maps are enabled (true).")
  }

}

object SbtUglify extends AutoPlugin {

  override def requires = SbtJsTask

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import SbtJsEngine.autoImport.JsEngineKeys._
  import SbtJsTask.autoImport.JsTaskKeys._
  import autoImport._
  import UglifyKeys._

  override def projectSettings = Seq(
    appDir := (resourceManaged in uglify).value / "app",
    buildDir := (resourceManaged in uglify).value / "build",
    comments := None,
    compress := true,
    compressOptions := Nil,
    define := None,
    enclose := false,
    excludeFilter in uglify := new SimpleFileFilter({
      f =>
        def fileStartsWith(dir: File): Boolean = f.getPath.startsWith(dir.getPath)
        fileStartsWith((resourceDirectory in Assets).value) || fileStartsWith((WebKeys.webModuleDirectory in Assets).value)
    }),
    includeFilter in uglify := GlobFilter("*.js"),
    resourceManaged in uglify := webTarget.value / uglify.key.label,
    mangle := true,
    mangleOptions := Nil,
    preamble := None,
    reserved := Nil,
    sourceMap := true,
    uglify := runOptimizer.dependsOn(webJarsNodeModules in Plugin).value
  )


  private def runOptimizer: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings =>

      val include = (includeFilter in uglify).value
      val exclude = (excludeFilter in uglify).value
      val optimizerMappings = mappings.filter(f => !f._1.isDirectory && include.accept(f._1) && !exclude.accept(f._1))

      SbtWeb.syncMappings(
        streams.value.cacheDirectory,
        optimizerMappings,
        appDir.value
      )

      val inputFiles = optimizerMappings.map(o => appDir.value / o._2)

      val options = Seq(
        comments.value,
        compress.value,
        compressOptions.value,
        define.value,
        enclose.value,
        (excludeFilter in uglify).value,
        (includeFilter in uglify).value,
        (resourceManaged in uglify).value,
        mangle.value,
        mangleOptions.value,
        preamble.value,
        reserved.value,
        sourceMap.value
      ).mkString("|")

      implicit val opInputHasher = OpInputHasher[File](f => OpInputHash.hashString(f.getAbsolutePath + "|" + options))
      val (outputFiles, _) = incremental.syncIncremental(streams.value.cacheDirectory / "run", inputFiles) {
        modifiedFiles: Seq[File] =>
          if (modifiedFiles.size > 0) {

            streams.value.log.info(s"Optimizing ${modifiedFiles.size} JavaScript(s) with Uglify")

            val nodeModulePaths = (nodeModuleDirectories in Plugin).value.map(_.getPath)
            val uglifyjsShell = (webJarsNodeModulesDirectory in Plugin).value / "uglify-js" / "bin" / "uglifyjs"


            val mangleArgs = if (mangle.value) {
              val stdArg = Seq("--mangle")
              val stdArgWithOptions = if (mangleOptions.value.isEmpty) stdArg else stdArg :+ mangleOptions.value.mkString(",")
              val reservedArgs = if (reserved.value.isEmpty) Nil else Seq("--reserved", reserved.value.mkString(","))
              stdArgWithOptions ++ reservedArgs
            } else {
              Nil
            }

            val compressArgs = if (compress.value) {
              val stdArg = Seq("--compress")
              if (compressOptions.value.isEmpty) stdArg else stdArg :+ compressOptions.value.mkString(",")
            } else {
              Nil
            }

            val defineArgs = define.value.map(Seq("--define", _)).getOrElse(Nil)

            val encloseArgs = if (enclose.value) Seq("--enclose") else Nil

            val commentsArgs = comments.value.map(Seq("--comments", _)).getOrElse(Nil)

            val preambleArgs = preamble.value.map(Seq("--preamble", _)).getOrElse(Nil)

            val commonArgs =
              mangleArgs ++
                compressArgs ++
                defineArgs ++
                encloseArgs ++
                commentsArgs ++
                preambleArgs

            val executeUglify = SbtJsTask.executeJs(
              state.value,
              (engineType in uglify).value,
              (command in uglify).value,
              nodeModulePaths,
              uglifyjsShell,
              _: Seq[String],
              (timeoutPerSource in uglify).value
            )

            modifiedFiles.foldLeft[(Map[File, OpResult], Unit)]((Map.empty, ())) {
              case (results, inputFile) =>
                val inputFileArgs = Seq(inputFile.getPath)

                val inputFilePath = inputFile.getPath
                val inputExtn = inputFilePath.drop(inputFilePath.lastIndexOf("."))
                val outputFile = buildDir.value / (inputFile.getPath.drop(appDir.value.getPath.size).dropRight(inputExtn.size) + ".min" + inputExtn)

                val outputFileArgs = Seq("--output", outputFile.getPath)

                val outputFileMap = file(outputFile.getPath + ".map")
                val sourceMapArgs = if (sourceMap.value) {
                  Seq(
                    "--source-map", outputFileMap.getPath,
                    "--source-map-url", outputFileMap.getName,
                    "--prefix", "relative"
                  )
                } else {
                  Nil
                }

                val args =
                  outputFileArgs ++
                    inputFileArgs ++
                    sourceMapArgs ++
                    commonArgs

                val success = executeUglify(args).headOption.fold(true)(_ => false)
                val result = if (success) {
                  val outputFiles = if (sourceMap.value) Set(outputFile, outputFileMap) else Set(outputFile)
                  (Map(inputFile -> OpSuccess(Set(inputFile), outputFiles)), ())
                } else {
                  (Map(inputFile -> OpFailure), ())
                }
                (results._1 ++ result._1, ())
            }

          } else {
            (Map.empty, Nil)
          }
      }

      (mappings.toSet ++ outputFiles.pair(relativeTo(buildDir.value))).toSeq
  }
}
