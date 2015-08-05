name := """Gifter"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  evolutions,
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  // anorm is now an external dependency (since play2.4)
  "com.typesafe.play" % "anorm_2.11" % "2.4.0",
  // use http://play-bootstrap3.herokuapp.com/
  "com.adrianhurt" % "play-bootstrap3_2.11" % "0.4.4-P24"
)

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"


// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
