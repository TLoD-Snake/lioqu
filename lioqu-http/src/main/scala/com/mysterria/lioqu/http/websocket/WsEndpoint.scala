package com.mysterria.lioqu.http.websocket

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{CompletionStrategy, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import scala.util.Failure

class WsEndpoint[M: ClassTag](
  wp: WiredProtocol[M],
  anactor: => WsActor[M],
  outBufferSize: Int = Int.MaxValue,
  overflowStrategy: OverflowStrategy = OverflowStrategy.fail.withLogLevel(Logging.ErrorLevel)
)(implicit as: ActorSystem) extends LazyLogging {
  import WsEndpoint._
  implicit val ec: ExecutionContext = as.dispatcher

  lazy private val actor = as.actorOf(Props(anactor))

  protected def actorInSink(connId: Long): Sink[WsActor.ProtocolMessage, NotUsed] = Sink
    .actorRef[WsActor.ProtocolMessage](
      ref = actor,
      onCompleteMessage = WsActor.Protocol.ConnectionClosed(connId),
      onFailureMessage = (t: Throwable) => WsActor.Protocol.ConnectionError(connId, t)
    )

  protected def endPointFlow(connId: Long): Flow[String, M, Any] = {
    val in =
      Flow[String]
        .map { s =>
          try {
            WsActor.Protocol.MessageReceived(connId, wp.thaw(s))
          } catch {
            case t: Throwable => WsActor.Protocol.ProtocolError(connId, t)
          }
        }
        .to(actorInSink(connId))

    val out = Source
      .actorRef[M](
        completionMatcher = CompleteonMatcher,
        failureMatcher = FailureMatcher,
        outBufferSize,
        overflowStrategy
      )
      .mapMaterializedValue(actor ! WsActor.Protocol.ConnectionOpened(connId, _))

    Flow.fromSinkAndSource(in, out)
  }

  def websocketFlow(connId: Long): Flow[Message, Message, Any] =
    Flow[Message]
      .collect {
        case TextMessage.Strict(msg) => msg
      }
      .via(endPointFlow(connId))
      .map {
        case msg: M => TextMessage.Strict(wp.freeze(msg))
        case msg: JsValue => TextMessage.Strict(Json.stringify(msg))
        case msg: String => TextMessage.Strict(msg)
        case msg => TextMessage.Strict(String.valueOf(msg))
      }
      .via(reportErrorsFlow)

  protected def reportErrorsFlow[T]: Flow[T, T, Any] =
    Flow[T]
      .watchTermination()((_, f) => f.onComplete {
        case Failure(cause) =>
          logger.error(s"WS stream failed with ${cause.getMessage}", cause)
        case _ =>
          logger.trace(s"Flow $this regular completion")
      })

}

object WsEndpoint {
  val CompleteonMatcher: PartialFunction[Any, CompletionStrategy] = {
    case akka.actor.Status.Success(s: CompletionStrategy) => s
    case akka.actor.Status.Success(_)                     => CompletionStrategy.immediately
    case akka.actor.Status.Success                        => CompletionStrategy.immediately
  }

  val FailureMatcher: PartialFunction[Any, Throwable] = {
    case akka.actor.Status.Failure(cause) => cause
  }
}
