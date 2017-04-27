package com.mysterria.lioqu.di

import javax.inject._
import com.mysterria.lioqu.service.DbMigrationService

class RepositoryModule extends LioquModule {
  override def configure(): Unit = {
    //bind[SomeRepository].to[SomeRepositoryImpl].in[Singleton]
    bind[DbMigrationService].in[Singleton]
  }
}
