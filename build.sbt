name := """Gifter"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  evolutions,
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  // anorm is now an external dependency (since play2.4)
  "com.typesafe.play" % "anorm_2.11" % "2.4.0",
  // use http://play-bootstrap3.herokuapp.com/
  "com.adrianhurt" % "play-bootstrap3_2.11" % "0.4.4-P24",
  // silouhette to handle google/yahoo authentification dude dude dude
  "com.mohiva" % "play-silhouette_2.11" % "3.0.1",
  // async postgres
  "com.kyleu" %% "jdub-async" % "1.0"
)

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"


// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
routesImport += "play.api.mvc.PathBindable.bindableUUID"
