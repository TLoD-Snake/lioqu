package com.mysterria.lioqu.commons.async

import com.mysterria.lioqu.commons.async.ConcurrentFutureProcessor
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.{Assertion, Succeeded}
import thesis._

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ConcurrentFutureProcessorTest extends AsyncFlatSpec {
  implicit override def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  "ConcurrentFutureProcessor" should "process tasks sequentially when concurrency == 1" in {
    val cfp = new ConcurrentFutureProcessor(concurrency = 1)
    val c = new AtomicInteger()
    val ints = for (_ <- 0 until 10) yield {
      cfp.add {
        Future {
          c.incrementAndGet()
        }
      }
    }
    val results = for (i <- 0 until 10) yield {
      ints(i).map{ r => assert(i == r-1) }
    }
    val asserts = Future.sequence(results).map(_ :+ assert(c.get == 10))
    asserts.map(_.forall(_ == Succeeded)).map(assert(_))
  }

  it should "not accept concurrency < 1" in {
    assertThrows[IllegalArgumentException]{ new ConcurrentFutureProcessor(concurrency = 0) }
    assertThrows[IllegalArgumentException]{ new ConcurrentFutureProcessor(concurrency = -1) }
  }

  it should "process tasks concurrently when concurrency > 1" in {
    val cfp = new ConcurrentFutureProcessor(concurrency = 10)
    val c = new AtomicInteger()
    val ints = for (_ <- 0 until 10) yield {
      cfp.add {
        Future {
          c.incrementAndGet()
        }
      }
    }
    Future.sequence(ints).map { _ =>
      assert(c.get == 10)
    }
  }

  it should "process failing futures" in {
    val cfp = new ConcurrentFutureProcessor(concurrency = 1)
    val e = new RuntimeException("Failed future")
    val result = cfp.add(Future.failed[Unit](e))
    result.mapAll[Assertion] {
      case Success(_) => throw new AssertionError("Must be failed")
      case Failure(t) => assertResult(e)(t)
    }
  }

  it should "process failing future providers" in {
    val cfp = new ConcurrentFutureProcessor(concurrency = 1)
    val e = new RuntimeException("Can't create a future")
    val result = cfp.add[Unit](throw e)
    result.mapAll[Assertion] {
      case Success(_) => throw new AssertionError("Must be failed")
      case Failure(t) => assertResult(e)(t)
    }
  }

  it should "process after previous future provider failure" in {
    val cfp = new ConcurrentFutureProcessor(concurrency = 1)
    cfp.add[Unit](throw new RuntimeException("Can't create a future"))
    cfp.add(Future { 122 } ) map { v => assertResult(122)(v) }
  }
}
