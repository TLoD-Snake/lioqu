package com.mysterria.lioqu.commons.async.queueprocessor

import scala.concurrent.Future

trait DiscardableQueueTask extends QueueTask {
  def discard(): Future[_]
}
