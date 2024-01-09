package com.mysterria.lioqu.commons.futures

import scala.concurrent.{ExecutionContext, Future}

/**
 * This cache doesn't guarantee that the `factory` method will only be called once per `TIn` value.
 */
class FuturesCache[TIn, TOut](name: String)(factory: (TIn) => Future[TOut])(implicit ec: ExecutionContext)
  extends PrefilledFuturesCache[TIn, TOut](name)(Future.successful(Map.empty[TIn, TOut]))(factory)
