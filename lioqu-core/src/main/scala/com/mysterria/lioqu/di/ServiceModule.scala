package com.mysterria.lioqu.di

import com.mysterria.lioqu.commons.service.ApplicationLifeCycle
import com.mysterria.lioqu.service.{LifeCycleService, PidService}

class ServiceModule extends LioquModule {
  override def configure(): Unit = {
    bind[ApplicationLifeCycle].to[LifeCycleService].asEagerSingleton()
    bind[PidService].asEagerSingleton()
  }
}
