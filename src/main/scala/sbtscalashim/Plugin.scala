package sbtscalashim

import sbt._

object Plugin extends sbt.Plugin {
  import Keys._
  import Project.Initialize

  lazy val scalaShim        = TaskKey[Seq[File]]("scalashim")

  private[this] def scalaShimFiles(dir: File, scalaVersion: String): Seq[File] = {
    val (major, minor) = parseScalaVersion(scalaVersion)
    
    if (major < 2 || minor < 9) Seq(fakeSysFile(dir))
    else Nil
  }

  private[this] def fakeSysFile(dir: File): File = {
    val f = dir / "fakesys.scala"
    val lines = """object sys {
      |  def error(message: String): Nothing = throw new RuntimeException(message)
      |
      |  def exit(): Nothing = exit(0)
      |  
      |  def exit(status: Int): Nothing = {
      |    java.lang.System.exit(status)
      |    throw new Throwable()
      |  }
      |}
      |""".stripMargin
    IO.write(f, lines)
    f
  }

  private[this] def parseScalaVersion(scalaVersion: String): (Int, Int) =
    try {
      val major :: minor :: xs = scalaVersion.split("""\.""").toList
      (major.toInt, minor.toInt) 
    }
    catch {
      case _ => (99, 99)
    }

  lazy val scalaShimSettings: Seq[Project.Setting[_]] = Seq(
    scalaShim <<= (sourceManaged in Compile, scalaVersion) map {
      (dir, scalaVersion) => scalaShimFiles(dir, scalaVersion)
    }
  )
}
