package com.mysterria.lioqu.http.websocket

import akka.actor.{ Actor, ActorRef, Status }
import com.typesafe.scalalogging.LazyLogging

import scala.reflect.ClassTag

abstract class WsActor[M: ClassTag]() extends Actor with LazyLogging {
  import WsActor._
  private var clients: Map[Long, ActorRef] = Map.empty

  override def receive: Receive = {
    case Protocol.ConnectionOpened(connId, client) =>
      logger.trace(s"Connection $connId opened for client $client")
      clients += connId -> client
      onConnectionOpened(connId)

    case Protocol.ConnectionClosed(connId) =>
      logger.trace(s"Connection $connId closed")
      clients.get(connId) foreach { actorRef =>
        actorRef ! Status.Success
        clients -= connId
      }
      onConnectionClosed(connId)

    case Protocol.ConnectionError(connId, cause) =>
      logger.trace(s"Connection $connId error: ${cause.getMessage}")
      clients.get(connId) foreach { actorRef =>
        actorRef ! Status.Success
        clients -= connId
      }
      onConnectionClosed(connId, Some(cause))

    case Protocol.MessageReceived(connId, message) =>
      logger.trace(s"Connection $connId sent a message $message")
      onMessageReceived(connId, message.asInstanceOf[M])

    case Protocol.ProtocolError(connId, cause) =>
      logger.warn(s"Connection $connId encountered protocol error: ${cause.getMessage}", cause)
      onProtocolError(connId, cause)
  }

  def dropAllExcept(except: Long, dropMessage: Option[Any] = None): Unit = {
    clients.view.filterKeys(_ != except).foreach {
      case (_, actorRef) =>
        dropMessage foreach { m => actorRef ! m }
        actorRef ! akka.actor.Status.Success
    }
  }

  def reply(connId: Long, message: Any): Unit = {
    clients.get(connId).foreach(_ ! message)
  }

  def replyAll(message: Any): Unit = {
    clients.foreach { case (_, c) => c ! message }
  }

  def drop(connId: Long, message: Option[Any] = None): Unit = {
    clients.get(connId).foreach { c =>
      message foreach { m => c ! m }
      c ! akka.actor.Status.Success
    }
  }

  def onConnectionOpened(connId: Long): Unit = ()
  def onConnectionClosed(connId: Long, error: Option[Throwable] = None): Unit = ()
  def onMessageReceived(connId: Long, message: M): Unit
  def onProtocolError(connId: Long, cause: Throwable): Unit = drop(connId)
}

object WsActor {
  sealed trait ProtocolMessage
  case object Protocol {
    case class ConnectionOpened(connId: Long, client: ActorRef) extends ProtocolMessage
    case class ConnectionClosed(connId: Long) extends ProtocolMessage
    case class ConnectionError(connId: Long, cause: Throwable) extends ProtocolMessage
    case class MessageReceived[M](connId: Long, m: M) extends ProtocolMessage
    case class ProtocolError(connId: Long, cause: Throwable) extends ProtocolMessage
  }
}
