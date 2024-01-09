package com.mysterria.lioqu.http.websocket

trait WiredProtocol[T] {
  def thaw(frozen: String): T
  def freeze(message: T): String
}
