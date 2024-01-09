import sbt.Resolver
logLevel := Level.Warn

// Release support (FOR ARTIFACT PROJECTS ONLY)
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.10")

// This one will give us a way to get to Amazon S3 if we decide to host artifacts there
resolvers += Resolver.jcenterRepo
addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.19.0")

