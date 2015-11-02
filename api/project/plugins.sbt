// Comment to get more information during initialization
logLevel := Level.Warn

resolvers += "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1.1")