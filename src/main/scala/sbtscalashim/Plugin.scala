package sbtscalashim

import sbt._

object Plugin extends sbt.Plugin {
  import Keys._
  import Project.Initialize

  lazy val scalaShim        = TaskKey[Seq[File]]("scalashim")

  private[this] def scalaShimFiles(dir: File, scalaVersion: String): Seq[File] = 
    parseScalaVersion(scalaVersion) match {
      case (2, 8, 0) => fake(dir, "fakesys_280.scala") :: Nil
      case (2, 8, x) => fake(dir, "fakesys_281.scala") :: Nil
      case _ => Nil
    }

  private[this] def fake(dir: File, fileName: String): File = {
    val f = dir / fileName
    IO.write(f, stringFromResource(fileName))
    f
  }

  private[this] def stringFromResource(path: String): String = {
    val in = getClass.getResourceAsStream("/" + path)
    val source = scala.io.Source.fromInputStream(in)
    source.toList.mkString("")
  }  

  private[this] def parseScalaVersion(scalaVersion: String): (Int, Int, Int) =
    try {
      val major :: minor :: release :: xs = scalaVersion.split("""\D""").toList
      (try { 
        major.toInt
      } catch {
        case _ => 99
      },
      try { 
        minor.toInt
      } catch {
        case _ => 99
      },
      try { 
        release.toInt
      } catch {
        case _ => 99
      })
    }
    catch {
      case _ => (99, 99, 99)
    }

  lazy val scalaShimSettings: Seq[Project.Setting[_]] = Seq(
    scalaShim <<= (sourceManaged in Compile, scalaVersion) map {
      (dir, scalaVersion) => scalaShimFiles(dir, scalaVersion)
    }
  )
}
