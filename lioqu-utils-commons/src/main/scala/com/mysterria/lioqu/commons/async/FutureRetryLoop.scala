package com.mysterria.lioqu.commons.async

import akka.actor.ActorSystem
import thesis._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

class FutureRetryLoop[T](op: => Future[T])
                        (retryDelay: Int => Option[FiniteDuration])
                        (logError: (Int, Option[FiniteDuration], Throwable) => Unit)
                        (implicit ec: ExecutionContext, as: ActorSystem) {
  private val resultPromise = Promise[T]()
  private val firstTryPromise = Promise[T]()
  val result = resultPromise.future
  val firstTry = firstTryPromise.future

  @volatile private var stopped = false

  iter()

  private def iter(retries: Int = 0): Unit = {
    op onComplete {
      case Success(res) =>
        resultPromise.trySuccess(res)

        if (retries == 0) {
          firstTryPromise.trySuccess(res)
        }

      case Failure(err) =>
        if (retries == 0) {
          firstTryPromise.tryFailure(err)
        }

        val nextDelay = !stopped ? retryDelay(retries) | None

        logError(retries, nextDelay, err)

        nextDelay match {
          case Some(delay) =>
            as.scheduler.scheduleOnce(delay) {
              iter(retries+1)
            }

          case None => resultPromise.tryFailure(err)
        }
    }
  }

  def stop() = stopped = true
}

object FutureRetryLoop {
  def runLoop[T](op: => Future[T])
                (retryDelay: Int => Option[FiniteDuration])
                (logError: (Int, Option[FiniteDuration], Throwable) => Unit)
                (implicit ec: ExecutionContext, as: ActorSystem): FutureRetryLoop[T] = {
    new FutureRetryLoop(op)(retryDelay)(logError)
  }

  def run[T](op: => Future[T])
            (retryDelay: Int => Option[FiniteDuration])
            (logError: (Int, Option[FiniteDuration], Throwable) => Unit)
            (implicit ec: ExecutionContext, as: ActorSystem): Future[T] = runLoop(op)(retryDelay)(logError).result
}