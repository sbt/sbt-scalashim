sbt-scalashim
=============

sbt-scalashim generates Scala shim like sys.error.

Latest
------

```scala
addSbtPlugin("com.eed3si9n" % "sbt-scalashim" % "0.2.1")

resolvers += Resolver.url("sbt-plugin-releases",
  new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
```

Usage
-----

Add the following in your `build.sbt`:

```scala
scalaShimSettings

sourceGenerators in Compile <+= scalaShim
```

When you reload the settings and compile, this generates a fake `sys` object under `scalashim` package for 2.8.x.
The supported methods are `sys.error`, `sys.exit`, `sys.runtime`, `sys.props`, `sys.env`, and `sys.allThreads`.

To use them, add `import scalashim._` in your source code.

To customize the package:

```scala
scalaShimPackage := "something"
```

License
-------

MIT License for sbt-scalashim code. Scala License for the generated `sys` code.
