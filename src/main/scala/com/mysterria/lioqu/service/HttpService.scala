package com.mysterria.lioqu.service

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.github.racc.tscg.TypesafeConfig
import com.mysterria.lioqu.config._

class HttpService @Inject()(
  lifeCycleService: LifeCycleService,
  routes: Routes,
  @TypesafeConfig(Http_Host) host: String,
  @TypesafeConfig(Http_Port) port: Int
)(implicit as: ActorSystem) {
  private implicit val materializer = ActorMaterializer()
  private implicit val dispatcher = as.dispatcher

  lifeCycleService.awaitAllReady foreach  { _ =>
    routes.routes map { r =>
      Http().bindAndHandle(r, host, port)
    }
  }
}
