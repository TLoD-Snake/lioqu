package com.mysterria.lioqu.db.migration.migrations.postgres

import java.sql.Connection

import com.mysterria.lioqu.db.migration.LioquInjectedMigration
import com.typesafe.scalalogging.LazyLogging
import org.flywaydb.core.api.migration.jdbc.JdbcMigration

class V1_1__inital extends JdbcMigration with LazyLogging {
  override def migrate(connection: Connection): Unit = {
    val st = connection.prepareStatement("CREATE TABLE zag_zag (zag VARCHAR(255) NOT NULL)")
    try {
      st.execute()
    } finally {
      st.close()
    }
  }
}
