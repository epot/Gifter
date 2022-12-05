import NativePackagerKeys._

name := """Gifter"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.3"

resolvers ++= Seq(
  "Atlassian Releases" at "https://maven.atlassian.com/public/",
  "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
)
resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
  ehcache,
  evolutions,
  guice,
  // anorm is now an external dependency (since play2.4)
  "org.playframework.anorm" %% "anorm" % "2.6.5",
  // silouhette to handle google/yahoo authentification dude dude dude
  "com.mohiva" %% "play-silhouette" % "7.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "7.0.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "7.0.0",
  "com.typesafe.play" %% "play-mailer" % "7.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "7.0.1",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.8.5-akka-2.6.x",
  "com.github.nscala-time" %% "nscala-time" % "2.22.0",
  "com.beachape" %% "enumeratum-play" % "1.5.16",
  // async postgres
  "org.postgresql" % "postgresql" % "42.2.8",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "6.1.1",
  "com.iheart" %% "ficus" % "1.4.7",
  "net.codingwell" %% "scala-guice" % "4.2.6",
  "com.typesafe.play" %% "play-slick" % "4.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.2",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.4.1",
  "com.datadoghq" % "java-dogstatsd-client" % "2.8.1",
  // emails
  "com.sendgrid" % "sendgrid-java" % "4.8.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.2"
)

excludeDependencies ++= Seq(
  // remove this bastard to avoid NoSuchMethodError: 'void com.zaxxer.hikari.HikariConfig.setInitializationFailTimeout(long)'
  ExclusionRule("com.zaxxer", "HikariCP-java6")
)

unmanagedResourceDirectories in Assets += baseDirectory.value / "app-ui" / "app" / "components" / "templates"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
routesImport += "play.api.mvc.PathBindable.bindableUUID"
