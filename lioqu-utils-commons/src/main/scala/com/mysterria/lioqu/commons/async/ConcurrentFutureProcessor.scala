package com.mysterria.lioqu.commons.async

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}
import thesis._
import java.util.concurrent.{ConcurrentLinkedQueue, Semaphore}

class ConcurrentFutureProcessor(concurrency: Int = 1)(implicit ec: ExecutionContext) extends AutoCloseable {
  if (concurrency < 1) throw new IllegalArgumentException("Concurrency must be > 0")
  private type Task = () => Future[_]
  private val q = new ConcurrentLinkedQueue[Task]()
  private val sem = new Semaphore(concurrency)
  @volatile
  private var stopped = false

  def close(): Unit = stopped = true

  def add[T](fp: => Future[T]): Future[T] = con(Promise[T]()) { p =>
    q.add(() => mkTask(p, fp))
    process()
  }.future

  private def mkTask[T](p: Promise[T], fp: => Future[T]): Future[T] = {
    if (stopped) {
      p.tryFailure(new IllegalStateException(s"$this is stopped"))
    } else {
      Try(fp) match {
        case Success(f) => f.onComplete(p.tryComplete)
        case Failure(t) => p.tryFailure(t)
      }
    }
    p.future
  }

  private def process(): Unit = {
    if (sem.tryAcquire()) {
      Option(q.poll()) match {
        case Some(t) => t() onComplete { _ =>
          sem.release()
          process()
        }
        case _ => sem.release()
      }
    }
  }
}
