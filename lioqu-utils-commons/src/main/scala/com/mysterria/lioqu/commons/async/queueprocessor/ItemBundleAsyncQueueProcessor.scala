package com.mysterria.lioqu.commons.async.queueprocessor

import java.util
import java.util.concurrent.LinkedBlockingDeque
import scala.jdk.CollectionConverters._

/**
  * Processes items from the queue in bundles whose size does not exceed set amount.
  */
trait ItemBundleAsyncQueueProcessor[T] extends AsyncQueueProcessor[T] {
  protected def maxBundleSize: Int

  override protected def selectItemsToProcess(q: LinkedBlockingDeque[T]): Seq[T] = {
    val list = new util.ArrayList[T]()

    if (q.drainTo(list, maxBundleSize) > 0) list.asScala.toSeq else Seq.empty
  }
}
