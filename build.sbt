name := """Gifter"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

resolvers ++= Seq(
  "Atlassian Releases" at "https://maven.atlassian.com/public/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)
resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
  cache,
  evolutions,
  // anorm is now an external dependency (since play2.4)
  "com.typesafe.play" %% "anorm" % "2.5.2",
  // use http://play-bootstrap3.herokuapp.com/
  "com.adrianhurt" %% "play-bootstrap" % "1.1-P25-B3",
  // silouhette to handle google/yahoo authentification dude dude dude
  "com.mohiva" %% "play-silhouette" % "4.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "4.0.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "4.0.0",
  "org.webjars" %% "webjars-play" % "2.5.0-2",
  "com.typesafe.play" %% "play-mailer" % "5.0.0",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.5.0-akka-2.4.x",
  "com.github.nscala-time" %% "nscala-time" % "2.14.0",
  // async postgres
  "com.kyleu" %% "jdub-async" % "1.0",
   jdbc,
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "4.0.0",
  "com.iheart" %% "ficus" % "1.2.6",
  "net.codingwell" %% "scala-guice" % "4.0.1"
)

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"


// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
routesImport += "play.api.mvc.PathBindable.bindableUUID"
