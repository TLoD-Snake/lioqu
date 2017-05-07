package com.mysterria.lioqu.service

import com.mysterria.lioqu.di.LioquCoreModule
import com.google.inject.Guice

object Main extends App {
  private val injector = Guice.createInjector(new LioquCoreModule)
}
