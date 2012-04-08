package sbtscalashim

import sbt._

object Plugin extends sbt.Plugin {
  import Keys._
  import Project.Initialize

  lazy val scalaShim        = TaskKey[Seq[File]]("scalashim")
  lazy val scalaShimPackage = SettingKey[String]("scalashim-package")

  private[this] def scalaShimFiles(dir: File, scalaVersion: String, pkg: String): Seq[File] = 
    parseScalaVersion(scalaVersion) match {
      case (2, 8, 0) => fake(dir, "fakesys_280.scala", pkg) :: Nil
      case (2, 8, x) => fake(dir, "fakesys_281.scala", pkg) :: Nil
      case _ => fake(dir, "fakesys.scala", pkg) :: Nil
    }

  private[this] def fake(dir: File, fileName: String, pkg: String): File = {
    val f = dir / fileName
    val s = stringFromResource(fileName).replaceAllLiterally("$package$", if (pkg == "") "" else "package " + pkg)
    IO.write(f, s)
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
    scalaShim <<= (sourceManaged in Compile, scalaVersion, scalaShimPackage) map {
      (dir, scalaVersion, pkg) => scalaShimFiles(dir, scalaVersion, pkg)
    },
    scalaShimPackage := "scalashim"
  )
}
