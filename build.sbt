sbtPlugin := true

name := "sbt-scalashim"

organization := "com.eed3si9n"

version := "0.2.0"

description := "sbt plugin to generate shim like sys.error"

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.html"))

seq(ScriptedPlugin.scriptedSettings: _*)

seq(lsSettings :_*)

LsKeys.tags in LsKeys.lsync := Seq("sbt", "codegen")

(externalResolvers in LsKeys.lsync) := Seq(
  "sbt-plugin-releases" at "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases")

publishArtifact in (Compile, packageBin) := true

publishArtifact in (Test, packageBin) := false

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false

publishMavenStyle := false

publishTo <<= (version) { version: String =>
   val scalasbt = "http://scalasbt.artifactoryonline.com/scalasbt/"
   val (name, u) = if (version.contains("-SNAPSHOT")) ("sbt-plugin-snapshots", scalasbt+"sbt-plugin-snapshots")
                   else ("sbt-plugin-releases", scalasbt+"sbt-plugin-releases")
   Some(Resolver.url(name, url(u))(Resolver.ivyStylePatterns))
}

credentials += Credentials(Path.userHome / ".ivy2" / ".sbtcredentials")
