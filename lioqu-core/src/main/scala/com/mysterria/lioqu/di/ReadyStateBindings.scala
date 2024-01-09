package com.mysterria.lioqu.di

import com.google.inject.{AbstractModule, Binder}
import com.mysterria.lioqu.commons.service.ReadyStateService
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}

trait ReadyStateBindings extends AbstractModule with ScalaModule {
  def readyStateBinder(binder: Binder): ScalaMultibinder[ReadyStateService] = ScalaMultibinder.newSetBinder[ReadyStateService](binder)
}
