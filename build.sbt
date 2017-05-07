import scala.language.postfixOps

val akkaVersion = "2.5.0"
val akkaHttpVersion = "10.0.5"
val slickVersion = "3.2.0"
val slickPgVersion = "0.15.0-RC"
val logBackVersion = "1.2.3"
val playJsonVersion = "2.6.0-M7"
val guiceVersion = "4.1.0"
val reflectionsVersion = "0.9.10"
val jodaVersion = "1.8.1"
val typesafeconfigVersion = "0.0.3"
val flywayVersion = "4.2.0"

lazy val commonSettings = Seq(
  organizationName := "mysterria",
  organization := "com.mysterria.lioqu",
  version := "0.1-SNAPSHOT",
  name := "lioqu",

  scalaVersion := "2.12.1",
  crossScalaVersions := Seq("2.12.1", "2.11.8"),

  scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation", "-explaintypes", "-encoding", "UTF8", "-Xlint", "-Xfatal-warnings",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:existentials"),

  resolvers ++= Seq(
    "mysterria.com artifactory - snapshot" at "http://artifactory.mysterria.com/artifactory/libs-snapshot-local",
    "mysterria.com artifactory - release" at "http://artifactory.mysterria.com/artifactory/libs-release-local"
  ),
  resolvers += Resolver.mavenLocal,

  publishTo := {
    val artifactory = "http://artifactory.mysterria.com"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at s"$artifactory/libs-snapshot-local;build.timestamp=" + new java.util.Date().getTime)
    else
      Some("releases" at s"$artifactory/libs-release-local")
  },
  credentials += Credentials(new File("credentials.properties")),
  publishMavenStyle := true,

  mainClass in (Compile, run) := Some("com.mysterria.lioqu.service.Main"),
  mainClass in (Compile, packageBin) := Some("com.mysterria.lioqu.service.Main")
)

def mainDependencies(scalaVersion: String) = {
  val extractedLibs = CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, scalaMajor)) if scalaMajor >= 12 =>
      Seq() // Use for Scala >= 2.12
    case _ =>
      Seq() // Use proprietary Future extensions for scala 2.11
  }
  Seq (
    // Akka
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,

    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,

    // JSON
    "com.typesafe.play" % "play-json_2.11" % playJsonVersion,

    // DB: drivers + Slick core
    "com.typesafe.slick" % "slick_2.11" % slickVersion,
    "com.typesafe.slick" % "slick-hikaricp_2.11" % slickVersion,
    "org.postgresql" % "postgresql" % "9.4.1208.jre7",

    // DB: Slick extensions
    "com.github.tminglei" % "slick-pg_2.11" % slickPgVersion,
    "com.github.tminglei" % "slick-pg_play-json_2.11" % slickPgVersion,
    "com.github.tminglei" % "slick-pg_joda-time_2.11" % slickPgVersion,

    // DB: Migrations
    "org.flywaydb" % "flyway-core" % flywayVersion,

    // Logging
    "ch.qos.logback" % "logback-classic" % logBackVersion,
    "com.typesafe.scala-logging" % "scala-logging_2.11" % "3.5.0",

    // Guice DI Container
    "net.codingwell" % "scala-guice_2.11" % guiceVersion,

    // Time and Date
    "org.joda" % "joda-convert" % jodaVersion,

    // Guice injected Config
    "com.github.racc" % "typesafeconfig-guice" % typesafeconfigVersion,

    // Reflections
    "org.reflections" % "reflections" % reflectionsVersion
  ) ++ extractedLibs
}

lazy val lioquCore = Project(id = "lioqu-core", base = file("./core"),
  settings = Defaults.coreDefaultSettings ++ commonSettings ++ Seq(
    name := "lioqu-core",
    description := "Core of Lioqu Microservice Framework",
    libraryDependencies := Seq(
      // add deps here
    ) ++ mainDependencies(scalaVersion.value)
  )
)

lazy val lioqu = Project(id = "lioqu", base = file("."),
  settings = Defaults.coreDefaultSettings ++ commonSettings ++ Seq(
    name := "lioqu",
    description := "Lioqu Microservice Framework",
    libraryDependencies := mainDependencies(scalaVersion.value)
  )
).dependsOn (lioquCore)





scalaVersion := "2.11.8"
scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation", "-explaintypes", "-encoding", "UTF8",
    "-Xlint", "-Xfatal-warnings")

resolvers += "Artifactory" at "http://localhost:8081/artifactory/jcenter"
publishTo := Some("Artifactory Realm" at "http://localhost:8081/artifactory/libs-snapshot-local;build.timestamp=" + new java.util.Date().getTime)
credentials += Credentials(new File("credentials.properties"))

