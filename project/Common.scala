

object Common {
  def version = "3.0.0-M9"
  def playVersion = System.getProperty("play.version", "2.6.12")
  def scalaVersion = System.getProperty("scala.version", "2.12.4")
  def crossScalaVersions = Seq(scalaVersion, "2.11.12")
}
