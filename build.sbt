// ------------------------------
// Automated code formatter before compilaion
// Disabled by default because this is confusing for beginners
//scalariformSettings

// ------------------------------
// for ./skinny test:coverage
// Disabled by default because scoverage 0.95.7 doesn't work with Skinny ORM
//ScoverageSbtPlugin.instrumentSettings

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

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2"

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"

libraryDependencies += "org.apache.commons" % "commons-compress" % "1.6"

libraryDependencies += "commons-io" % "commons-io" % "2.4"
