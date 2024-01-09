package com.mysterria.lioqu.commons

import akka.actor.Actor
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

trait LazyActorLogging {
  this: Actor =>

  protected lazy val logger: Logger =
    Logger(LoggerFactory.getLogger(s"${getClass.getName}.${self.path.name}"))
}