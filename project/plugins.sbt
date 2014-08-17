// --------------------------------------------
// sbt plugins for this Skinny app project
// --------------------------------------------
resolvers += Classpaths.sbtPluginReleases

// --------
// Scalatra sbt plugin
addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.5" excludeAll(
  ExclusionRule(organization = "org.mortbay.jetty"),
  ExclusionRule(organization = "org.eclipse.jetty"),
  ExclusionRule(organization = "org.apache.tomcat.embed"),
  ExclusionRule(organization = "com.earldouglas")
))

// scalatra-sbt depends on xsbt-web-plugin
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.9.0" excludeAll(
  ExclusionRule(organization = "org.mortbay.jetty"),
  ExclusionRule(organization = "org.eclipse.jetty"),
  ExclusionRule(organization = "org.apache.tomcat.embed")
))

// for Scalate template compilaion
addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.5.0")

// --------
// scalarifrom for code formatting
// NOTE: Disabled by default because this is confusing for beginners
//
//addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

// --------
// IntelliJ IDEA
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// --------
// scoverage for test coverage (./skinny test:coverage)
addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.99.7.1")

// --------
// scalac options for sbt
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

// addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.6")
// addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

