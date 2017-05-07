package com.mysterria.lioqu.di

import com.mysterria.lioqu.service.{HttpService, Routes}

class HttpModule extends LioquModule {
  override def configure(): Unit = {
    bind[Routes]
    readyStateBinder(binder).addBinding.to[Routes]
    bind[HttpService].asEagerSingleton
  }
}
