package com.mysterria.lioqu.commons.async.queueprocessor

import java.util.concurrent.LinkedBlockingDeque

import scala.concurrent.Future

/**
  * `AsyncQueueProcessor` which processes items one at a time.
  */
trait SingleItemAsyncQueueProcessor[T] extends AsyncQueueProcessor[T] {
  /**
    * Processes one item.
    */
  protected def processItem(item: T): Future[_]

  override protected def selectItemsToProcess(q: LinkedBlockingDeque[T]): Seq[T] = {
    Option(queue.poll()).toSeq
  }

  override protected def processItems(items: Seq[T]): Future[_] = {
    if (items.size != 1) {
      throw new IllegalArgumentException(s"${getClass.getSimpleName} can only process one item at a time, but got ${items.size} instead.")
    }

    processItem(items.head)
  }
}
