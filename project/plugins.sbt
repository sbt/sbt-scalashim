resolvers += Resolver.url("Typesafe repository", 
  url("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)

libraryDependencies <+= (sbtVersion) { sv =>
  // "org.scala-sbt" %% "scripted-plugin" % sv
  "org.scala-sbt" % "scripted-plugin" % sv
}

resolvers ++= Seq(
  "less is" at "http://repo.lessis.me",
  "coda" at "http://repo.codahale.com")

// addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.1")
