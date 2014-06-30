sbt-uglify
==========

[![Build Status](https://api.travis-ci.org/sbt/sbt-uglify.png?branch=master)](https://travis-ci.org/sbt/sbt-uglify)

An SBT plugin to perform [UglifyJs optimization](http://lisperator.net/uglifyjs).

To use this plugin use the addSbtPlugin command within your project's `plugins.sbt` file:

    addSbtPlugin("com.typesafe.sbt" % "sbt-uglify" % "1.0.3")

Your project's build file also needs to enable sbt-web plugins. For example with build.sbt:

    lazy val root = (project in file(".")).enablePlugins(SbtWeb)

As with all sbt-web asset pipeline plugins you must declare their order of execution e.g.:

```scala
pipelineStages := Seq(uglify)
```

A standard build profile for the Uglify optimizer is provided which will mangle variables for obfuscation and 
compress. Each input `.js` file found in your assets folders will have a corresponding `.min.js` file and source maps are also generated. 
If you wish to limit or extend what is uglified then you can use filters e.g.:

```scala
includeFilter in uglify := GlobFilter("myjs/*.js"),
```

...where the above will include only those files under the `myjs` folder. The sbt `excludeFilter` is also available 
to the `uglify` scope and defaults to excluding the public folder and extracted Webjars.

You are able to use and/or customize settings already made, and add your own. Here are a list of relevant settings and
their meanings (please refer to the [UglifyJs documentation](http://lisperator.net/uglifyjs) for details on the 
options):

Option                  | Description
------------------------|------------
comments                | Specifies comments handling. Defaults to None.
compress                | Enables compression. The default is to compress. Set to false to not compress.
compressOptions         | A sequence of options for compression such as hoist_vars, if_return etc.
define                  | Define globals. Defaults to None.
enclose                 | Enclose in one big function. Defaults to false.
mangle                  | Enables name mangling. The default is to mangle.
mangleOptions           | Options for mangling such as sort, topLevel etc.
preamble                | Any preamble to include at the start of the output. Defaults to None.
reserved                | Reserved names to exclude from mangling.
sourceMap               | Enables source maps. The default is that source maps are enabled (true).
uglifyOps               | A function defining how to combine input files into output files, taking the list of included inputs and returning the list of output files that should be generated along with their sources. Defaults to a one-to-one mapping, uglifying each file.js to file.min.js separately.")

The plugin is built on top of [JavaScript Engine](https://github.com/typesafehub/js-engine) which supports different JavaScript runtimes.

&copy; Typesafe Inc., 2014
