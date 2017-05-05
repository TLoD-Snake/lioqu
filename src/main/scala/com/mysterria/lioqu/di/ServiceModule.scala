package com.mysterria.lioqu.di

import com.mysterria.lioqu.db.migration.DbMigrationService
import com.mysterria.lioqu.service.LifeCycleService

class ServiceModule extends LioquModule {
  override def configure(): Unit = {
    bind[LifeCycleService].asEagerSingleton()
  }
}
