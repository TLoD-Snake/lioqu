package com.mysterria.lioqu.di

import scala.concurrent.Future

trait ReadyStateService {
  def ready: Future[Unit]
}
