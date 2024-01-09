package com.mysterria.lioqu.commons.grpc

object GrpcAppThreadPool extends Enumeration {
  type GrpcAppThreadPool = Value

  val Akka, Fixed, Default = Value
}