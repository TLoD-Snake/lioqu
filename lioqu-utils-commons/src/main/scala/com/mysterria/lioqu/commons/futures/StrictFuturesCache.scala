package com.mysterria.lioqu.commons.futures

import scala.concurrent.{ExecutionContext, Future}

/**
 * This cache guarantees that the `factory` method will only be called once per `TIn` value.
 */
class StrictFuturesCache[TIn, TOut](factory: (TIn) => Future[TOut])(implicit ec: ExecutionContext) {
  private val opLock = new AnyRef()
  private val cache = scala.collection.mutable.HashMap[TIn, Future[TOut]]()

  def get(in: TIn): Future[TOut] = {
    cache.getOrElse(in, {
      opLock synchronized {
        cache.getOrElseUpdate(in, {
          val result = factory(in)

          result.failed foreach  { _ =>
            opLock synchronized {
              cache.get(in).foreach { f =>
                if (f == result) {
                  cache.remove(in)
                }
              }
            }
          }

          result
        })
      }
    })
  }
}