package com.mysterria.lioqu.db.migration.migrations.postgres

import javax.inject._

import com.mysterria.lioqu.db.connection.PgDBComponent
import com.mysterria.lioqu.db.migration.LioquInjectedMigration
import com.mysterria.lioqu.repo.TestDummyRepository
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

class V1_3__injected @Inject()(testDummyRepository: TestDummyRepository) extends LioquInjectedMigration with PgDBComponent with LazyLogging {
  def migrate(): Future[Unit] = {
    testDummyRepository.ddl
  }
}
