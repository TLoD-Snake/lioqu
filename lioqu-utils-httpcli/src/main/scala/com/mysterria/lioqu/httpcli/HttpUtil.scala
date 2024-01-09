package com.mysterria.lioqu.httpcli

import com.mysterria.lioqu.commons.TimeLoggingSettings
import java.util.concurrent.atomic.AtomicLong
import com.typesafe.scalalogging.{LazyLogging, Logger}
import com.mysterria.lioqu.commons.logging.LogHelpers._
import dispatch.{Http, url}
import thesis._

import scala.concurrent.{ExecutionContext, Future}

object HttpUtil extends LazyLogging {

  private val requestCounter = new AtomicLong()

  def req[TBody, TResult](
    serverUri: String,
    method: String,
    path: String,
    contentType: String,
    body: TBody = null,
    additionalHeaders: Iterable[(String, String)] = Seq()
  )(
    responseHandler: (Int, String) => TResult
  )(
    errorHandler: (Exception, Int, String, String) => TResult
  )(implicit ec: ExecutionContext, logger: Logger, timeLoggingSettings: TimeLoggingSettings): Future[TResult] = {
    val requestId = requestCounter.incrementAndGet()

    var cli = url(serverUri + "/" + path)
      .addHeader("Content-Type", s"$contentType; charset=UTF-8")
      .addHeader("Connection", "close")
      .setMethod(method) <:< additionalHeaders

    if (body != null) {
      val bodyStr = body.toString

      cli = cli.setBody(bodyStr)

      logger.debug(log"Sending $method request #$requestId to $serverUri/$path with body $bodyStr")
    }

    val start = System.currentTimeMillis
    Http.default(cli) map { resp =>
      var respBody: String = "<N/A>"
      var statusCode: Int = -1

      try {
        statusCode = resp.getStatusCode
        respBody = resp.getResponseBody

        __log_time__(start, s"$method request #$requestId to $serverUri/$path")

        logger.debug(log"Got response to $method #$requestId $serverUri/$path, code = $statusCode, body = $respBody")

        responseHandler(statusCode, respBody)
      } catch {
        case err: Exception => errorHandler(err, statusCode, respBody, resp.getHeader("Content-Type"))
      }
    }
  }
}
