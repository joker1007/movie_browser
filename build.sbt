// ------------------------------
// for ./skinnny package:standalone
import AssemblyKeys._

assemblySettings

mainClass in assembly := Some("skinny.standalone.JettyLauncher")

_root_.sbt.Keys.test in assembly := {}

// ------------------------------
// for ./skinny scoverage:test

ScoverageSbtPlugin.instrumentSettings

// ------------------------------
// Disabled by default because this is confusing for beginners
//scalariformSettings

// ------------------------------
// for ./skinnny console
initialCommands := """
import _root_.controller._, model._
import org.joda.time._
import scalikejdbc._, SQLInterpolation._, config._
DBsWithEnv("development").setupAll()
"""

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2"

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"

libraryDependencies += "org.apache.commons" % "commons-compress" % "1.6"

libraryDependencies += "commons-io" % "commons-io" % "2.4"
