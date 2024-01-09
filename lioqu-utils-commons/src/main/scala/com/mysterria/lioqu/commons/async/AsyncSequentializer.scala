package com.mysterria.lioqu.commons.async

import java.util.concurrent.atomic.AtomicReference

import thesis._

import scala.concurrent.{ExecutionContext, Future, Promise}

class AsyncSequentializer(implicit ec: ExecutionContext) {
  private val tail = new AtomicReference[Future[_]](Future.successful(()))

  def nextAction[T](action: => Future[T]): Future[T] = {
    // create next promise
    val promise = Promise[T]()

    // get current tail and set the new one
    val prev = tail.getAndSet(promise.future)

    // attach an action to the previous tail future
    prev flatMapAll { case _ =>
      action
    } onComplete promise.complete

    promise.future
  }
}