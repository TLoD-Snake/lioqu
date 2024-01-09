package com.mysterria.lioqu.commons.async.queueprocessor

import scala.concurrent.Future

trait QueueTask {
  def execute(): Future[_]
}
