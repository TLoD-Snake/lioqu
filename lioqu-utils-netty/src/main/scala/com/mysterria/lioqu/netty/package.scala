package com.mysterria.lioqu

import io.netty.util.concurrent.{FutureListener, Future => NFuture}

import scala.concurrent.{Future, Promise}

package object netty {
  implicit class NettyFutureExtensions[T](nf: NFuture[T]) {
    def asScala: Future[T] = {
      val p = Promise[T]()
      val nfl = new FutureListener[T] {
        override def operationComplete(future: NFuture[T]): Unit =
          if (future.isSuccess) p.trySuccess(future.get()) else p.tryFailure(future.cause())
      }
      nf.addListener(nfl)
      p.future
    }
  }
}
