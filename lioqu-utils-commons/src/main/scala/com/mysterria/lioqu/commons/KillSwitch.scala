package com.mysterria.lioqu.commons

import scala.concurrent.{ExecutionContext, Future}

/**
  * You should redefine ONLY ONE method for each action.
  * It means you should redefine either `restart` or `restartSync` method for restart action and
  * either `stop` or `stopSync` method for stop action.
  *
  * _Sync methods will be called ONLY if their async counterparts were kept intact.
  */
trait KillSwitch {
  def restart(message: Option[String] = None, cause: Option[Throwable] = None): Future[Unit] =
    Future.successful(restartSync(message, cause))
  
  protected def restartSync(message: Option[String] = None, cause: Option[Throwable] = None): Unit =
    throw new UnsupportedOperationException(s"restart operation is not supported in $this")

  def restart(message: String, cause: Throwable): Future[Unit] = restart(Some(message), Some(cause))
  def restart(message: String): Future[Unit] = restart(Some(message))
  def restart(cause: Throwable): Future[Unit] = restart(cause = Some(cause))

  def stop(message: Option[String] = None, cause: Option[Throwable] = None): Future[Unit] =
    Future.successful(stopSync(message, cause))

  protected def stopSync(message: Option[String] = None, cause: Option[Throwable] = None): Unit =
    throw new UnsupportedOperationException(s"stop operation is not supported in $this")

  def stop(message: String, cause: Throwable): Future[Unit] = stop(Some(message), Some(cause))
  def stop(message: String): Future[Unit] = stop(Some(message))
  def stop(cause: Throwable): Future[Unit] = stop(cause = Some(cause))
}

object KillSwitch {
  def apply(
    restartOp: (Option[String], Option[Throwable]) => Future[_] = $nop,
    stopOp: (Option[String], Option[Throwable]) => Future[_] = $nop
  )(implicit ec: ExecutionContext): KillSwitch = new KillSwitch {
    override def restart(message: Option[String], cause: Option[Throwable]): Future[Unit] =
      restartOp(message, cause).map(_ => ())

    override def stop(message: Option[String], cause: Option[Throwable]): Future[Unit] =
      stopOp(message, cause).map(_ => ())
  }

  def sync(
    restartOp: (Option[String], Option[Throwable]) => Unit = $nopSync,
    stopOp: (Option[String], Option[Throwable]) => Unit = $nopSync
  ): KillSwitch = new KillSwitch {
    override protected def restartSync(message: Option[String], cause: Option[Throwable]): Unit =
      restartOp(message, cause)

    override protected def stopSync(message: Option[String], cause: Option[Throwable]): Unit =
      stopOp(message, cause)
  }

  private val $nop: (Option[String], Option[Throwable]) => Future[_] = (_, _) => Future.successful(())
  private val $nopSync: (Option[String], Option[Throwable]) => Unit = (_, _) => ()
}