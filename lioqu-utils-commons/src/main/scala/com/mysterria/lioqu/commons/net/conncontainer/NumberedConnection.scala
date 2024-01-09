package com.mysterria.lioqu.commons.net.conncontainer

case class NumberedConnection[T](number: Long, connection: T) {
  override def toString: String = s"NumberedConnection[#$number, conn=$connection]"
}