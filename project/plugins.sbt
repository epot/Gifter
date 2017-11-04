// Comment to get more information during initialization
logLevel := Level.Warn

// additional repositories
resolvers ++= Seq(
  "Atlassian Releases" at "https://maven.atlassian.com/public/",
  "JCenter repo" at "https://bintray.com/bintray/jcenter/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.6")


