package com.mysterria.lioqu.connection

import slick.basic.DatabaseConfig

trait DbConfigProvider[P <: slick.basic.BasicProfile] {
  def conf: DatabaseConfig[P]
  def schema: Option[String]
}
