name := "helloworld"

version := "0.1"

scalaVersion := "2.8.0"

crossScalaVersions := Seq("2.8.0", "2.8.1", "2.9.1")

scalaShimSettings

scalaShimPackage := "scalashim"

sourceGenerators in Compile <+= scalaShim
