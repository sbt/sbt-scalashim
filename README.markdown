sbt-scalashim
=============

sbt-scalashim generates Scala shim like sys.error.

Latest
------

```scala
addSbtPlugin("com.eed3si9n" % "sbt-scalashim" % "0.1.0")

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

When you reload the settings and compile, this generates the following only if `scalaVersion` is prior to 2.9:

```scala
object sys {
  def error(message: String): Nothing = throw new RuntimeException(message)

  def exit(): Nothing = exit(0)
  
  def exit(status: Int): Nothing = {
    java.lang.System.exit(status)
    throw new Throwable()
  }
}
```

License
-------

MIT License
