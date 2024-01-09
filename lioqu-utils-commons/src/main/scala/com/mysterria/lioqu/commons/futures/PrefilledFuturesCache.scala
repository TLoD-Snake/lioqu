package com.mysterria.lioqu.commons.futures

import java.util.concurrent.ConcurrentHashMap

import com.typesafe.scalalogging.LazyLogging
import com.mysterria.lioqu.commons.logging.LogHelpers._
import thesis._

import scala.jdk.CollectionConverters._
import scala.collection.concurrent
import scala.concurrent.{ExecutionContext, Future}

class PrefilledFuturesCache[TIn, TOut](name: String)
                                      (prefill: => Future[Map[TIn, TOut]])
                                      (factory: (TIn) => Future[TOut])
                                      (implicit ec: ExecutionContext) extends LazyLogging {
  logger.debug(log"Prefilling $name ...")
  private val startPrefill = System.currentTimeMillis()

  private val cacheFuture: Future[concurrent.Map[TIn, Future[TOut]]] = prefill.map { prefillMap =>
    val map = new ConcurrentHashMap[TIn, Future[TOut]]().asScala

    map ++= prefillMap.map { case (k, v) =>
      k -> Future.successful(v)
    }

    logger.info(log"Prefilled $name with ${prefillMap.size} items in ${System.currentTimeMillis()-startPrefill} ms")

    map
  } recover {
    case err =>
      logger.warn(log"Failed to prefill $name, using empty initial set", err)

      new ConcurrentHashMap[TIn, Future[TOut]]().asScala
  }

  def get(in: TIn): Future[TOut] = {
    cacheFuture.flatMap { cache =>
      cache.computeIfAbsent(in, { _ =>
        val result = factory(in)

        result.failed foreach { _ =>
          cache.remove(in, result)
        }

        result
      })
    }
  }

  def reset(in: TIn): Unit = {
    cacheFuture.foreach(_.remove(in))
  }
}