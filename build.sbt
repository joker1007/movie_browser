// ------------------------------
// Automated code formatter before compilaion
// Disabled by default because this is confusing for beginners
//scalariformSettings

// ------------------------------
// for ./skinny test:coverage
// https://github.com/scoverage/sbt-scoverage
instrumentSettings

// ------------------------------
// for ./skinnny console
initialCommands := """
import skinny._
import _root_.controller._, model._
import org.joda.time._
import scalikejdbc._, SQLInterpolation._, config._
DBSettings.initialize()
"""

// ------------------------------
// sbt-dependency-graph
//net.virtualvoid.sbt.graph.Plugin.graphSettings

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.5" % "test",
  "org.apache.commons" % "commons-compress" % "1.6",
  "commons-io" % "commons-io" % "2.4",
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "io.argonaut" %% "argonaut" % "6.1-M4"
)
