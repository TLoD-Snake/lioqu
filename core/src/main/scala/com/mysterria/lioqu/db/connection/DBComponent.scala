package com.mysterria.lioqu.db.connection

import slick.basic.{BasicProfile, DatabaseConfig}

trait DBComponent[P <: BasicProfile] {
  val profile: P

  def dbConfigPath: String

  lazy val db: P#Backend#Database = provider.conf(dbConfigPath).db

  val schema: Option[String] = provider.schema(dbConfigPath)

  def provider: DBConfigProvider[P]
}

trait DBConfigProvider [P <: slick.basic.BasicProfile] {
  def conf(s: String): DatabaseConfig[P]
  def schema(configPath: String): Option[String]
}


