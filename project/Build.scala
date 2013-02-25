import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "WindsorData"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
     "org.apache.poi" % "poi" % "3.9",
     "org.apache.poi" % "poi-ooxml" % "3.9",
     "org.apache.poi" % "poi-ooxml-schemas" % "3.9",
     "junit" % "junit" % "4.8.1" % "test",
     "org.scalatest" % "scalatest_2.9.1" % "1.8" % "test",
    jdbc,
    anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
