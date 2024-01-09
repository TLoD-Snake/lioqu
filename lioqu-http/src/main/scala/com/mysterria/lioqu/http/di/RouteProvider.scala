package com.mysterria.lioqu.http.di

import akka.http.scaladsl.server.Route

trait RouteProvider {
  def route: Route
}
