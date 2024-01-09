package com.mysterria.lioqu.commons.async.queueprocessor

import java.util.concurrent.LinkedBlockingDeque

import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import thesis._

trait AsyncQueueProcessor[T] {
  implicit protected val ec: ExecutionContext

  protected def logger: Logger

  private var isProcessing = false
  private val processingLock = new AnyRef()
  protected val queue = new LinkedBlockingDeque[T]()

  protected def stopped: Boolean

  /**
    * Processes a bunch of items and returns Future of when processing is complete.
    */
  protected def processItems(items: Seq[T]): Future[_]

  /**
    * Discards all pending items after queue processor was stopped. By default it does nothing.
    */
  protected def discardItems(items: Seq[T]): Future[_] = Future.successful(())

  /**
    * Selects which items are gonna be processed in the next iteration.
    * This method should use locally supplied queue and is also guaranteed to run only on one thread at a time.
    */
  protected def selectItemsToProcess(q: LinkedBlockingDeque[T]): Seq[T]

  protected def addToQueue(item: T): Unit = {
    if (stopped) throw new IllegalStateException("Queue is already stopped")
    queue.add(item)

    processQueue()
  }

  protected def processQueue(): Unit = {
    val nextItems: Seq[T] = processingLock synchronized {
      if (!isProcessing) {
        con(selectItemsToProcess(queue)) { items =>
          isProcessing = items.nonEmpty
        }
      } else Seq.empty
    }

    if (nextItems.nonEmpty) {
      processItemsImpl(nextItems)
    }
  }

  private def processItemsImpl(items: Seq[T]): Unit = {
    val processor: () => Future[_] = if (!stopped) () => processItems(items) else () => discardItems(items)

    val future: Future[_] = Try(processor()) match {
      case Success(res) => res
      case Failure(err) => Future.failed(err)
    }

    future onComplete { _ =>
      selectItemsToProcess(queue) match {
        case nextItems if nextItems.nonEmpty =>
          processItemsImpl(nextItems)

        case _ =>
          isProcessing = false
          processQueue()
      }
    }
  }
}
