package com.mysterria.lioqu

package object config {
  final val Prefix = "lioqu."

  final val Http_Host = Prefix + "http.host"
  final val Http_Port = Prefix + "http.port"
  final val Db_Postgres = Prefix + "postgres"
  final val Db_Schema = "db.schema"

  /**
    * Reflections will use this value to scan for annotations
    * Should be properly set up during project initialization via 'sbt new' command
    * if tlod-snake/lioqu-template.g8 template used
    */
  final val AppPackage = Prefix + "app_package"
}
