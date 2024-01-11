import sbt.Keys.{javaOptions, publishTo}

import scala.language.postfixOps

lazy val commonSettings = Seq(
  scalaVersion := "2.13.7",

  scalacOptions ++= Seq(
    "-feature", "-unchecked", "-deprecation", "-explaintypes", "-encoding", "UTF8",
    "-Xlint",
    //"-Xfatal-warnings", // Disabled because of https://github.com/scala/bug/issues/10134
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:existentials"
  ),

  // Publishing
  organizationName := "MYSTERRIA Inc.",
  organization := "com.mysterria.lioqu",
  licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
  developers := List(Developer(
    "tlod-snake",
    "Igor Zinkovsky aka TLoD_Snake",
    "admin@mysterria.com",
    url("https://www.mysterria.com")
  )),
  scmInfo := Some(ScmInfo(
    url("http://github.com/TLoD-Snake/lioqu/tree/master"),
    "scm:git:git://github.com/TLoD-Snake/lioqu.git",
    "scm:git:ssh://github.com:TLoD-Snake/lioqu.git"
  )),
  publishMavenStyle := true,
  versionScheme := Some("semver-spec"),

  // SBT-Release plugin publish step should call signed publish version
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,

  publishTo := {
    val path = if (isSnapshot.value) "snapshots" else "releases"
    Some(MavenCache("local-maven", file(s"/home/snake/work/maven-repo/$path")))
  },

  javaOptions in Test ++=
      collection.JavaConverters.propertiesAsScalaMap(System.getProperties)
    .map{ case (key,value) => "-D" + key + "=" +value }.toSeq,
)

/*
==============================================================================================
==================== Dependencies ============================================================
==============================================================================================
 */

val slickVersion = "3.3.3"
val slickPgVersion = "0.19.7"
val akkaVersion = "2.6.17"
val playJsonVersion = "2.9.2"
val nettyVersion = "4.1.75.Final"


def mainDependencies(scalaVersion: String) = {
  val extractedLibs = CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, scalaMajor)) if scalaMajor >= 12 =>
      Seq() // Use for Scala >= 2.12
    case _ =>
      Seq() // Use proprietary Future extensions for scala 2.11
  }
  Seq (
    // Logging
    "ch.qos.logback"                %  "logback-classic"        % "1.2.13",
    "com.typesafe.scala-logging"    %% "scala-logging"          % "3.9.4",
    "com.github.dwickern"           %% "scala-nameof"           % "3.0.0" % "provided",

    "org.scalatest" %% "scalatest" % "3.2.10" % Test,
    "org.mockito" %% "mockito-scala" % "1.16.46" % Test
  ) ++ extractedLibs
}

/**
  * This project is neither aggregated by the root Lioqu project nor published as an artifact.
  */
//lazy val utilsLang = (project in file("lioqu-utils-lang"))
//  .settings(
//    commonSettings,
//    name := "lioqu-utils-lang",
//    description := "Macros project for Lioqu Microservice Framework"
//  )

lazy val commons = (project in file("lioqu-utils-commons"))
  .settings(
    commonSettings,
    name := "lioqu-utils-commons",
    description := "Common utils library for Lioqu Microservice Framework",
    libraryDependencies := Seq(

      "javax.inject"        %  "javax.inject" % "1",
      "commons-codec"       %  "commons-codec"  % "1.15",

      "com.typesafe.play" %% "play-json"    % playJsonVersion,
      "com.typesafe.akka" %% "akka-actor"   % akkaVersion
    ) ++ mainDependencies(scalaVersion.value)
  )

lazy val utilsDi = project
  .settings(
    commonSettings,
    name := "lioqu-utils-di",
    description := "DI dependencies",
    libraryDependencies := Seq(
      // DI - Guice
      "com.google.inject"             %  "guice"                          % "5.0.1",
      "com.google.inject.extensions"  %  "guice-assistedinject"           % "5.0.1",
      "net.codingwell"                %% "scala-guice"                    % "5.0.2"
    ) ++ mainDependencies(scalaVersion.value)
  )

lazy val utilsHttpCli = (project in file("lioqu-utils-httpcli"))
  .settings(
    commonSettings,
    name := "lioqu-utils-httpcli",
    description := "HTTP Client based on Dispatch library",
    libraryDependencies := Seq(
      "org.dispatchhttp"            %% "dispatch-core"                  % "1.2.0"
    ) ++ mainDependencies(scalaVersion.value)
  ).dependsOn(commons)

lazy val core = (project in file("lioqu-core"))
  .settings(
    commonSettings,
    name := "lioqu-core",
    description := "Core of Lioqu Microservice Framework",
    mainClass in (Compile, run) := Some("com.mysterria.lioqu.service.Main"),
    mainClass in (Compile, packageBin) := Some("com.mysterria.lioqu.service.Main"),
    libraryDependencies := Seq(
      // Reflections
      "org.reflections" % "reflections" % "0.10.2"
    ) ++ mainDependencies(scalaVersion.value)
  ).dependsOn(commons, utilsDi)

lazy val http = (project in file("lioqu-http"))
  .settings(
    commonSettings,
    name := "lioqu-http",
    description := "HTTP module of Lioqu Microservice Framework",
    libraryDependencies := Seq(
      "com.typesafe.akka" %% "akka-stream"  % akkaVersion,
      "com.typesafe.akka" %% "akka-http"    % "10.2.7"
    ) ++ mainDependencies(scalaVersion.value)
  )
  .dependsOn(core)

lazy val dbCore = (project in file("lioqu-db-core"))
  .settings(
    commonSettings,
    name := "lioqu-db-core",
    description := "Core DB module of Lioqu Microservice Framework",
    libraryDependencies := Seq(
      // DB: drivers + Slick core
      "com.typesafe.slick" %% "slick"          % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
    ) ++ mainDependencies(scalaVersion.value)
  )
  .dependsOn(core)

lazy val dbPostgres = (project in file("lioqu-db-postgres"))
  .settings(
    commonSettings,
    name := "lioqu-db-postgres",
    description := "Postgres DB module of Lioqu Microservice Framework",
    libraryDependencies := Seq(
      "org.postgresql"      % "postgresql"     % "42.3.1",
      // DB: Slick extensions
      "com.github.tminglei" %% "slick-pg"           % slickPgVersion,
      "com.github.tminglei" %% "slick-pg_play-json" % slickPgVersion,
      "com.github.tminglei" %% "slick-pg_joda-time" % slickPgVersion

      //"com.jsuereth"        %% "scala-arm"          % "2.0" % Test
    ) ++ mainDependencies(scalaVersion.value)
  )
  .dependsOn(dbCore)

lazy val dbMysql = (project in file("lioqu-db-mysql"))
  .settings(
    commonSettings,
    name := "lioqu-db-mysql",
    description := "Mysql DB module of Lioqu Microservice Framework",
    libraryDependencies := Seq(
      "mysql" % "mysql-connector-java" % "8.0.27"
    ) ++ mainDependencies(scalaVersion.value)
  )
  .dependsOn(dbCore)

lazy val netty = (project in file("lioqu-utils-netty"))
  .settings(
    commonSettings,
    name := "lioqu-utils-netty",
    description := "Toolbox for using Netty in Scala",
    libraryDependencies := Seq(
      "io.netty" % "netty-common" % nettyVersion intransitive()
    ) ++ mainDependencies(scalaVersion.value)
  )
  .dependsOn(commons)
/*
  This subproject is currently not aggregated by Lioqu.
  Pending for real life usage cases.
 */
lazy val dbMigration = (project in file("lioqu-db-migration"))
  .settings(
    commonSettings,
    name := "lioqu-db-migration",
    description := "DB migrations module of Lioqu Microservice Framework",
    libraryDependencies := Seq(
      "org.flywaydb" % "flyway-core" % "8.0.3"
    ) ++ mainDependencies(scalaVersion.value)
  )
  .dependsOn(dbCore, dbPostgres)

lazy val lioqu = (project in file("."))
  .settings(
    commonSettings,
    name := "lioqu",
    description := "Lioqu Microservice Framework"
  )
  .aggregate(
    commons, utilsHttpCli, utilsDi,
    core, http, dbCore, dbPostgres, dbMysql
  )
