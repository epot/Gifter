import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "Gifter"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      // Add your project dependencies here,
      jdbc,
      anorm,
      "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    )

}
