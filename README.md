sbt-uglify
==========

[![Build Status](https://api.travis-ci.org/sbt/sbt-uglify.png?branch=master)](https://travis-ci.org/sbt/sbt-uglify)

An SBT plugin to perform [UglifyJs optimization](http://lisperator.net/uglifyjs).

To use this plugin use the addSbtPlugin command within your project's `plugins.sbt` file:

    addSbtPlugin("com.typesafe.sbt" % "sbt-uglify" % "1.0.0")

Your project's build file also needs to enable sbt-web plugins. For example with build.sbt:

    lazy val root = (project in file(".")).enablePlugins(SbtWeb)

As with all sbt-web asset pipeline plugins you must declare their order of execution e.g.:

```scala
pipelineStages := Seq(uglify)
```

A standard build profile for the Uglify optimizer is provided which will mangle variables for obfuscation and 
compress. Source maps are also generated. Also by default, everything is minified into a "main.min.js" file either
in your `js` or `javascripts` folder, or in the root of your assets if neither of those exist.

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
output                  | The target relative url path for Uglify output. Defaults to ./main.min.js".
preamble                | Any preamble to include at the start of the output. Defaults to None.
reserved                | Reserved names to exclude from mangling.
sourceMap               | Enables source maps. The default is that source maps are enabled (true).

Supposing that your application does not have a `main.js` and consequently `main.min.js` makes no sense. Suppose
that instead, it should be `js/app.min.js` given the existence of `js/app.js`:

```scala
UglifyKeys.output := "js/app.min.js"
```

The plugin is built on top of [JavaScript Engine](https://github.com/typesafehub/js-engine) which supports different JavaScript runtimes.

&copy; Typesafe Inc., 2014
