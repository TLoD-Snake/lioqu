package com.mysterria.lioqu.commons.async

import java.util.concurrent.ConcurrentHashMap

import thesis._

import scala.jdk.CollectionConverters._
import scala.concurrent.{ExecutionContext, Future, Promise}

class AsyncSequentializerMap[TKey](implicit ec: ExecutionContext) {
  private val tails = new ConcurrentHashMap[TKey, Future[Unit]]().asScala

  def nextAction(key: TKey)(action: => Future[Unit]): Unit = {
    val promise = Promise[Unit]()

    var prevTailOpt: Option[Future[Unit]] = null
    do {
      prevTailOpt = tails.get(key)
    } while (
      prevTailOpt map { prevTail =>
        !tails.replace(key, prevTail, promise.future)
      } getOrElse {
        tails.putIfAbsent(key, promise.future).isDefined
      }
    ) // CAS

    (prevTailOpt getOrElse Future.successful(())) flatMapAll { case _ =>
      action
    } onComplete { case _ =>
      promise.trySuccess(())
    }
  }
}