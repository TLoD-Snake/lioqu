package com.mysterria.lioqu.db.connection

import com.mysterria.lioqu.commons.con
import com.mysterria.lioqu.config.Db_Schema
import com.typesafe.scalalogging.LazyLogging
import slick.basic.{BasicProfile, DatabaseConfig}

import scala.reflect.ClassTag
import scala.util.Try

object GenericDBConfigProvider extends LazyLogging {

  /*
    TODO: Implement DBConfigCache not to load config many times during init
    TODO: Implement configPath -> schema cache
  */

  def conf[P <: slick.basic.BasicProfile : ClassTag](configPath: String): DatabaseConfig[P] = {
    con(DatabaseConfig.forConfig[P](configPath)) { c =>
      logger.debug(s"DB initialized with config ${c.config}")
    }
  }

  def basicConf(configPath: String): DatabaseConfig[BasicProfile] = conf[BasicProfile](configPath)

  def schema(configPath: String): Option[String] = {
    schema(conf[BasicProfile](configPath))
  }

  def schema(dbConfig: DatabaseConfig[_]): Option[String] = {
    Try{dbConfig.config.getString(Db_Schema)}.toOption
  }
}
