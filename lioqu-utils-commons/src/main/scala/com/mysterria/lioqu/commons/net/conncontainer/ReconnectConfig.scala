package com.mysterria.lioqu.commons.net.conncontainer

import thesis._
import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration

case class ReconnectConfig(
  timeoutStep: FiniteDuration,
  timeoutMax: FiniteDuration,
  connectTimeout: Option[FiniteDuration],
  clientConnectionWaitMax: Option[FiniteDuration],
  extraFlags: Option[Config] = None) {

  def reqExtraFlags: Config = extraFlags.getOrThrow(s"Required Extra Flags are missing!")

  override def toString: String =
    s"${getClass.getSimpleName}[timeoutStep=$timeoutStep, timeoutMax=$timeoutMax, connectTimeout=$connectTimeout, clientConnectionWaitMax=$clientConnectionWaitMax]"
}

object ReconnectConfig {
  def fromConfig(config: Config) = ReconnectConfig(
    config.getDuration("delay-step"),
    config.getDuration("delay-max"),
    if (config.hasPath("connect-timeout")) Some(config.getDuration("connect-timeout")) else None,
    if (config.hasPath("client-connection-wait-max")) Some(config.getDuration("client-connection-wait-max")) else None,
    Some(config)
  )
}