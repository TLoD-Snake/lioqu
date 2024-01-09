package com.mysterria.lioqu.db.postgres.connection

import com.mysterria.lioqu.db.connection.{DBComponent, DBConfigProvider, GenericDBConfigProvider}
import com.mysterria.lioqu.db.postgres.config.Db_Postgres
import com.typesafe.scalalogging.LazyLogging
import slick.basic.DatabaseConfig


trait PgDBComponent extends DBComponent[SlickPgProfile] {
  override def dbConfigPath: String = Db_Postgres

  override val profile: SlickPgProfile = SlickPgProfile

  override def provider: DBConfigProvider[SlickPgProfile] = PgConfigProvider
}

object PgConfigProvider extends DBConfigProvider[SlickPgProfile] with LazyLogging {
  def conf(configPath: String): DatabaseConfig[SlickPgProfile] = GenericDBConfigProvider.conf[SlickPgProfile](configPath)
  def schema(configPath: String): Option[String] = GenericDBConfigProvider.schema(configPath)
}
