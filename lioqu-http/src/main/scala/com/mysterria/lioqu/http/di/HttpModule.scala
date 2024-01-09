package com.mysterria.lioqu.http.di

import com.mysterria.lioqu.di.{AppModule, LioquModule}
import com.mysterria.lioqu.http.Server
import com.mysterria.lioqu.http.config.LioquHttpConfig
import com.typesafe.config.Config

@AppModule
class HttpModule(config: Config) extends LioquModule with RouteBindings {
  override def configure(): Unit = {
    routeBinder(binder) // Init
    bind[LioquHttpConfig].toInstance(LioquHttpConfig(config))
    bind[Server].asEagerSingleton()
  }

}
