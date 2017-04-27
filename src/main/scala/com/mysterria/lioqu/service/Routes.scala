package com.mysterria.lioqu.service

import javax.inject._

import akka.http.scaladsl.server.Route
import com.mysterria.lioqu.di.ReadyStateService
import com.mysterria.lioqu.json.JsonHelper

import scala.concurrent.{ExecutionContext, Future}

class Routes @Inject()(implicit ec: ExecutionContext) extends JsonHelper with ReadyStateService {
  override def ready: Future[Unit] = Future.successful(())
  val routes: Option[Route] = None
}
