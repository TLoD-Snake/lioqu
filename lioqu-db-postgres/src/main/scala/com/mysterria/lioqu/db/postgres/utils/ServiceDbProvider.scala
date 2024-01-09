package com.mysterria.lioqu.db.postgres.utils

import slick.basic.DatabaseConfig

trait ServiceDbProvider {
  def dbConf: DatabaseConfig[SlickPgDriver]
}
