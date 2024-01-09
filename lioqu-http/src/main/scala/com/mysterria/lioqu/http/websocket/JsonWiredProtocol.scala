package com.mysterria.lioqu.http.websocket

import play.api.libs.json.{Format, Json}

class JsonWiredProtocol[T: Format] extends WiredProtocol[T] {
  override def thaw(frozen: String): T = Json.parse(frozen).as[T]
  override def freeze(message: T): String = Json.stringify(Json.toJson(message))
}