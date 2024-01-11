logLevel := Level.Warn

// Release support (FOR ARTIFACT PROJECTS ONLY)
//addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.10")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.3.0")

// Publishing
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.10.0")


