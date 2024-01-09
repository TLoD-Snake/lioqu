package com.mysterria.lioqu.db.migration

import com.typesafe.config.Config

import scala.util.Try

case class MigrationDescriptor(
  name: String,
  dbConfigPath: String,
  location: String,
  enabled: Boolean = true,
  baselineOnMigrate: Boolean = true
)

object MigrationDescriptor {
  def fromConfig(name: String, c: Config): MigrationDescriptor = {
    MigrationDescriptor(
      name = name,
      dbConfigPath = c.getString("configPath"),
      enabled = Try(c.getBoolean("enabled")).getOrElse(true),
      baselineOnMigrate = Try(c.getBoolean("baselineOnMigrate")).getOrElse(true),
      location = c.getString("location")
    )
  }
}