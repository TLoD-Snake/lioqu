package com.mysterria.lioqu.commons.net.conncontainer

import java.util.concurrent.atomic.AtomicReference

import akka.actor.ActorSystem

import scala.concurrent.{ExecutionContext, Future, Promise}
import thesis._
import com.mysterria.lioqu.commons.logging.LogHelpers._
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success}

abstract class ReconnectingContainer[T](rt: ReconnectTimeout)(implicit ec: ExecutionContext, as: ActorSystem) extends LazyLogging {

  private val connectionRef = new AtomicReference[Future[T]](connect())
  private val stopSync = new AnyRef
  private var stopped = false

  def connection: Future[T] = connectionRef.get()

  def isStopped: Boolean = stopSync synchronized stopped

  protected def startConnection(): Future[T]
  protected def stopConnection(connection: T): Future[Unit]
  protected def disconnectedFuture(connection: T): Future[Unit]

  /**
    * Fires when connection was successfully established
    */
  protected def onConnectionAcquired(conn: T): Unit = ()

  private def connect(tryCnt: Int = 0): Future[T] = {
    con(startConnection()) { _ onComplete {
      case Success(conn) =>
        onConnectionAcquired(conn)
        disconnectedFuture(conn) foreach {
          _ => stopSync synchronized {
            if (!stopped) {
              logger.warn(log"$this unexpected disconnect occured, will reconnect immediately")
              connectionRef.set(connect())
            }
          }
        }
      case Failure(t) => stopSync synchronized {
        if (!stopped) connectionRef.set(scheduledConnect(t, tryCnt + 1))
      }
    }}

  }

  private def scheduledConnect(prevError: Throwable, tryCnt: Int): Future[T] = {
    val timeout = rt.timeout(tryCnt)

    logger.warn(log"$this connect failed, tries: $tryCnt, retry in $timeout", prevError)

    val p = Promise[T]()
    as.scheduler.scheduleOnce(timeout) {
      connect(tryCnt).onComplete(p.complete)
    }
    p.future
  }

  def stop(): Future[Unit] = stopSync synchronized {
    stopped = true
    connectionRef.get() flatMapAll {
      case Success(s) => stopConnection(s)
      case Failure(_) => Future.successful(())
    }
  }
}
