package com.mysterria.lioqu.di

import javax.inject._

import com.mysterria.lioqu.db.migration.DbMigrationService
import com.mysterria.lioqu.repo.{TestDummyRepository, TestDummyRepositoryImpl}

class RepositoryModule extends LioquModule {
  override def configure(): Unit = {
    bind[TestDummyRepository].to[TestDummyRepositoryImpl].in[Singleton]
  }
}
