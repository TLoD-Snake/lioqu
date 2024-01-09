package com.mysterria.lioqu.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.mysterria.lioqu.http.di.RouteProvider
import com.mysterria.lioqu.http.config.LioquHttpConfig
import com.typesafe.scalalogging.LazyLogging
import javax.inject.{Inject, Named}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class Server @Inject() (
  config: LioquHttpConfig,
  @Named("routes") routeProviders: Set[RouteProvider])(implicit ec: ExecutionContext, as: ActorSystem) extends RoutesHelper with LazyLogging {

  private val (interface, port) = (config.bindHost, config.bindPort)

  //Http().bindAndHandle(foldRoutes(routeProviders), interface, port) onComplete  {
  Http().newServerAt(interface, port).bindFlow(foldRoutes(routeProviders)) onComplete  {
    case Success(binding) => logger.info(s"Server online at 'http://$interface:$port/'; binding: $binding")
    case Failure(t) => logger.error(s"Unable to start server on 'http://$interface:$port/': ${t.getMessage}", t)
  }
}