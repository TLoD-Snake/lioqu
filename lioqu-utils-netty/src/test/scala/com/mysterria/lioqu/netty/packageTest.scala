package com.mysterria.lioqu.netty

import io.netty.util.concurrent.{CompleteFuture, FailedFuture, FutureListener, GlobalEventExecutor, Future => NFuture}
import org.scalatest.flatspec.AsyncFlatSpec
import thesis._

class packageTest extends AsyncFlatSpec {
  "testNettyFutureExtensions" should "work with failed future" in {
    val nettyFailedFuture: NFuture[Unit] = new FailedFuture(GlobalEventExecutor.INSTANCE, new Exception())
    val scalaFuture = nettyFailedFuture.asScala
    scalaFuture.mapAll(r => assert(r.isFailure))
  }

  "testNettyFutureExtensions" should "work with completed future" in {
    val nf: NFuture[Unit] = new CompleteFuture[Unit](GlobalEventExecutor.INSTANCE) {
      override def isSuccess: Boolean = true
      override def cause(): Throwable = null
      override def getNow: Unit = ()
    }
    val scalaFuture = nf.asScala
    scalaFuture.mapAll(r => assert(r.isSuccess))
  }
}
