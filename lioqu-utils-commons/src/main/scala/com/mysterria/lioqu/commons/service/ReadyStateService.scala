package com.mysterria.lioqu.commons.service

import scala.concurrent.Future

trait ReadyStateService {
  def ready: Future[Unit] = Future.successful(())
  def terminate(): Future[Unit] = Future.successful(())
}