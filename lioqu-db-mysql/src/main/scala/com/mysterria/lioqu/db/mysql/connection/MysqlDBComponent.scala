package com.mysterria.lioqu.db.mysql.connection

import com.mysterria.lioqu.db.connection.{DBComponent, DBConfigProvider, GenericDBConfigProvider}
import com.mysterria.lioqu.db.mysql.config._
import com.typesafe.scalalogging.LazyLogging
import slick.basic.DatabaseConfig

trait MysqlDBComponent extends DBComponent[SlickMysqlProfile] {
  override val profile: SlickMysqlProfile = SlickMysqlProfile

  override def dbConfigPath: String = Db_Mysql

  override def provider: DBConfigProvider[SlickMysqlProfile] = MysqlConfigProvider
}

object MysqlConfigProvider extends DBConfigProvider[SlickMysqlProfile] with LazyLogging {
  def conf(configPath: String): DatabaseConfig[SlickMysqlProfile] = GenericDBConfigProvider.conf[SlickMysqlProfile](configPath)
  def schema(configPath: String): Option[String] = GenericDBConfigProvider.schema(configPath)
}
