package com.mysterria.lioqu.db.mysql.connection

import slick.jdbc.JdbcProfile

trait SlickMysqlProfile extends JdbcProfile {
  override val api: API = SlickMysqlAPI

  object SlickMysqlAPI extends API
}

object SlickMysqlProfile extends SlickMysqlProfile