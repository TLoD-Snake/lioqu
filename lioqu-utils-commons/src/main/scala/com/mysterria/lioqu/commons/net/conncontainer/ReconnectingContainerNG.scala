package com.mysterria.lioqu.commons.net.conncontainer

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

import akka.actor.ActorSystem
import com.mysterria.lioqu.commons.exceptions.{ClientConnectionWaitTimeoutException, ConnectTimeoutException}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.mysterria.lioqu.commons.logging.LogHelpers._
import thesis._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

abstract class ReconnectingContainerNG[T](
  protected val rt: ReconnectConfig,
  firstDisconnectIsCritical: Boolean = true)(implicit as: ActorSystem) extends LazyLogging {

  logger.info(log"Started with reconnect config: $rt")

  private implicit val ec: ExecutionContext = as.dispatcher
  private val connectionNumberHolder = new AtomicLong(0L)
  private val stopSync = new AnyRef
  @volatile private var stopped = false
  private val connectionRef: AtomicReference[Future[NumberedConnection[T]]] = new AtomicReference()
  stopSync synchronized { connectionRef.set(connect()) }

  /**
    * @param rtConfig - Config object with 'delay-step' and 'delay-max' keys holding Duration values. See ReconnectTimeout class for details
    */
  def this(rtConfig: Config)(implicit as: ActorSystem) = {
    this(ReconnectConfig.fromConfig(rtConfig))
  }

  /**
    * !!! This method is called from the ReconnectingContainer constructor, so please don't use any descendant state in it.
    */
  protected def startConnection(tryCnt: Int, lastConnected: Option[T]): Future[T]
  protected def stopConnection(connection: T): Future[Unit]
  protected def disconnectedFuture(connection: T): Future[Unit]
  protected def testConnection(connection: T): Boolean = true

  /**
    * @deprecated Please use `connection` method instead
    */
  final def session: Future[T] = connection

  final def connection: Future[T] = {
    val curConnectionFuture = connectionRef.get()

    val p = Promise[T]()

    val timeout = rt.clientConnectionWaitMax map { duration =>
      as.scheduler.scheduleOnce(duration) {
        p.tryFailure(new ClientConnectionWaitTimeoutException(s"$this client waited for connection #${connectionNumberHolder.get} for more then $duration"))
      }
    }

    curConnectionFuture onComplete {
      case Success(nconn) =>
        if (testConnection(nconn.connection)) {
          p.trySuccess(nconn.connection)
          timeout.foreach(_.cancel())
        } else {
          logger.info(log"Connection $nconn check on acquire failed")
          reconnectAfterClose(nconn) onComplete { res =>
            p.tryComplete(res.map(_.connection))
            timeout.foreach(_.cancel())
          }
        }
      case Failure(t) =>
        p.tryFailure(t)
    }

    p.future
  }

  private def connect(tryCnt: Int = 0, lastConnection: Option[T] = None): Future[NumberedConnection[T]] = stopSync synchronized {
    val connectionNumber = connectionNumberHolder.incrementAndGet()
    logger.info(log"$this starting connection #$connectionNumber")

    val newConnectionFuture = try {
      startConnection(tryCnt, lastConnection)
    } catch {
      case t: Throwable => Future.failed(t)
    }

    val resultPromise = Promise[T]()
    val timeout = rt.connectTimeout map { connectionEstablishTimeout =>
      as.scheduler.scheduleOnce(connectionEstablishTimeout) {
        val exception = new ConnectTimeoutException(s"$this connection #$connectionNumber establishment took more then limit of $connectionEstablishTimeout")
        if (resultPromise.tryFailure(exception)) newConnectionFuture foreach { conn =>
          logger.info(log"$this connection #$connectionNumber is stopping as being established after timeout (conn = $conn)")
          stopConnection(conn)
        }
      }
    }
    newConnectionFuture onComplete { r =>
      resultPromise.tryComplete(r)
      timeout.foreach(_.cancel())
    }

    resultPromise.future onComplete {
      case Success(conn) =>
        logger.info(log"$this opened new connection #$connectionNumber (conn = $conn)")
        disconnectedFuture(conn) onComplete {
          case Success(_) => reconnectAfterClose(conn, connectionNumber)
          case Failure(t) => reconnectAfterClose(conn, connectionNumber, Some(t))
        }
      case Failure(t) =>
        connectionRef.set(reconnectAfterFailure(t, tryCnt + 1, lastConnection, connectionNumber))
    }


    resultPromise.future.map(f => NumberedConnection(connectionNumber, f))
  }

  private final def reconnectAfterClose(prevConnection: T, connectionNumber: Long, cause: Option[Throwable] = None): Future[NumberedConnection[T]] = stopSync synchronized {
    if (!stopped) {
      val currentConnFuture = connectionRef.get
      if (currentConnFuture.isCompleted) {
        Try(Await.result(currentConnFuture, 0.second)).toOption map { curNumberedConn =>
          if (prevConnection == curNumberedConn.connection && connectionNumber == curNumberedConn.number) {
            val causeText = cause map { t => s"; Cause: ${t.getMessage}" } getOrElse ""
            if (firstDisconnectIsCritical) {
              logger.error(log"$this connection #$connectionNumber $prevConnection was closed unexpectedly$causeText", cause.orNull)
            } else {
              logger.debug(log"$this connection #$connectionNumber $prevConnection was closed unexpectedly$causeText")
            }
            con(connect(0, Some(prevConnection)))(connectionRef.set(_))
          } else {
            logger.trace(log"Prev connection ${NumberedConnection(connectionNumber, prevConnection)} differs from current $curNumberedConn. Curent one will be returned.")
            currentConnFuture
          }
        } getOrElse connectionRef.get()
      } else currentConnFuture
    } else {
      con(ReconnectingContainerNG.stoppedFuture)(connectionRef.set(_))
    }
  }

  private final def reconnectAfterClose(nconn: NumberedConnection[T]): Future[NumberedConnection[T]] = reconnectAfterClose(nconn.connection, nconn.number)

  private final def reconnectAfterFailure(prevError: Throwable, tryCnt: Int, lastConnection: Option[T], connectionNumber: Long): Future[NumberedConnection[T]] = stopSync synchronized {
    if (!stopped) {
      scheduledConnect(prevError, tryCnt, lastConnection, connectionNumber)
    } else {
      logger.error(log"$this connect #$connectionNumber attempt failed, tries: $tryCnt, error: ${prevError.getMessage}, no further reconnects since container is stopped.")
      ReconnectingContainerNG.stoppedFuture
    }
  }

  private final def scheduledConnect(prevError: Throwable, tryCnt: Int, lastConnected: Option[T], connectionNumber: Long): Future[NumberedConnection[T]] = {
    val nextTimeout = rt.timeoutStep * tryCnt
    val timeout = (rt.timeoutMax > nextTimeout) ? nextTimeout | rt.timeoutMax

    logger.error(log"$this connect #$connectionNumber attempt failed, tries: $tryCnt, retry in $timeout, error: ${prevError.getMessage}", prevError)

    val p = Promise[NumberedConnection[T]]()
    as.scheduler.scheduleOnce(timeout) {
      connect(tryCnt, lastConnected).onComplete(p.complete)
    }
    p.future
  }

  def stop(): Future[Unit] = stopSync synchronized {
    logger.info(log"$this container stopped by request")
    stopped = true
    connectionRef.get() flatMapAll {
      case Success(s) => stopConnection(s.connection)
      case Failure(_) => Future.successful(())
    }
  }

  def close(): Future[Unit] = stop()
}

object ReconnectingContainerNG {
  private def stoppedFuture = Future.failed(new Exception("Connection container stopped"))
}
