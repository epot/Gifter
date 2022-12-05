// Comment to get more information during initialization
logLevel := Level.Warn

// additional repositories
resolvers ++= Seq(
  "Atlassian Releases" at "https://maven.atlassian.com/public/",
  "JCenter repo" at "https://bintray.com/bintray/jcenter/",
  "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
)

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.9.9")


