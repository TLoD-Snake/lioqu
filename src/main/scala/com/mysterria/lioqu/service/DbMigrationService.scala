package com.mysterria.lioqu.service

import javax.inject._
import com.mysterria.lioqu.di.ReadyStateService
import scala.concurrent.{ExecutionContext, Future}

class DbMigrationService @Inject()()(implicit ec: ExecutionContext) extends ReadyStateService {
  override def ready: Future[Unit] = Future.successful(())
}
