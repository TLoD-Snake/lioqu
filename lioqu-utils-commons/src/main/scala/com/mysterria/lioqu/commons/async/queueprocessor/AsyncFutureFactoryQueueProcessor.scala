package com.mysterria.lioqu.commons.async.queueprocessor

import AsyncFutureFactoryQueueProcessor.AsyncDiscardableQueueTask

import scala.concurrent.{Future, Promise}
import scala.util.Try
import thesis._

/**
  * Special implementation of queue processor which queues up actions which are started when the time comes.
  * This allows us to serialize access to resources which can't be used from multiple threads simultaneously.
  *
  * Analogous in many ways to `AsyncSequentializer` but is able to track the number of pending Futures.
  */
trait AsyncFutureFactoryQueueProcessor extends SingleItemAsyncQueueProcessor[DiscardableQueueTask] {

  override protected def processItem(item: DiscardableQueueTask): Future[_] = item.execute()

  override protected def discardItems(items: Seq[DiscardableQueueTask]): Future[_] = {
    whenAll[Any, Seq](items.map(_.discard()))
  }

  /**
    * Adds the supplied synchronous action to the execution queue.
    */
  protected def queuedSync[R](action: => R): Future[R] = {
    val promise = Promise[R]()
    val wrap = () => {
      Future {
        promise.complete(Try(action))
      }
    }
    Try(addToQueue(new AsyncDiscardableQueueTask(wrap, promise))).failed.foreach(promise.tryFailure)
    promise.future
  }

  /**
    * Adds the supplied asynchronous action to the exection queue.
    */
  protected def queued[R](action: => Future[R]): Future[R] = {
    val promise = Promise[R]()
    val wrap = () => {
      try {
        action onComplete promise.complete
      } catch {
        case err: Throwable => promise.failure(err)
      }
      promise.future
    }
    Try(addToQueue(new AsyncDiscardableQueueTask(wrap, promise))).failed.foreach(promise.tryFailure)
    promise.future
  }
}

object AsyncFutureFactoryQueueProcessor {
  class AsyncDiscardableQueueTask(task: () => Future[_], promise: Promise[_]) extends DiscardableQueueTask {
    override def execute(): Future[_] = task()

    override def discard(): Future[_] = {
      Future.successful(
        promise.tryFailure(new RuntimeException("Discarded due to queue shutdown."))
      )
    }
  }
}

