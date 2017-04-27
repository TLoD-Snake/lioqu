package com.mysterria.lioqu.connection

import com.mysterria.lioqu.config._
import com.mysterria.lioqu.commons._
import com.typesafe.scalalogging.LazyLogging
import slick.basic.DatabaseConfig

import scala.util.{Failure, Success, Try}

trait DBComponent {
  //TODO: Change it to Lioqu Generic DB Profile with all extensions defined
  val profile = SlickPgProfile

  import profile.api._
  lazy val db: Database = PgDBConfigProvider.conf.db

  def schema: Option[String] = None
}

object PgDBConfigProvider extends DbConfigProvider[SlickPgProfile] with LazyLogging {
  val conf = con(DatabaseConfig.forConfig[SlickPgProfile](Db_Postgres)) { c =>
    logger.info(s"DB initialized with config ${c.config}")
  }

  val schema: Option[String] = Try(conf.config.getString(Db_Schema)) match {
    case Success(s) => Some(s)
    case Failure(t) => throw new RuntimeException("For Postgres shema MUST be difined in DB config. Use 'public' if you're not sure.", t)
  }
}
