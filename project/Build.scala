import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "axle-gameweb"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "se.radley" %% "play-plugins-salat" % "1.3.0",
    "org.pingel" %% "axle-core" % "0.1-SNAPSHOT",
    "org.pingel" %% "axle-games" % "0.1-SNAPSHOT",
    "mysql" % "mysql-connector-java" % "5.1.24",
    "org.apache.zookeeper" % "zookeeper" % "3.4.5" excludeAll(
      ExclusionRule(organization = "com.sun.jdmk"),
      ExclusionRule(organization = "com.sun.jmx"),
      ExclusionRule(organization = "javax.jms")
    )
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
      routesImport += "se.radley.plugin.salat.Binders._",
      templatesImport += "org.bson.types.ObjectId",
      resolvers += "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )

}
