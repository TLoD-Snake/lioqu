package com.mysterria.lioqu.db.migration.migrations.mysql

import java.sql.Connection

import org.flywaydb.core.api.migration.jdbc.JdbcMigration

class V1_1__inital extends JdbcMigration {

  override def migrate(connection: Connection): Unit = {
    throw new RuntimeException("Oups, this should never be executed!")
  }

}
