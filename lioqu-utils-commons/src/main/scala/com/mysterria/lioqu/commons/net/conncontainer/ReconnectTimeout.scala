package com.mysterria.lioqu.commons.net.conncontainer

import com.typesafe.config.Config
import scala.concurrent.duration.FiniteDuration
import thesis._

case class ReconnectTimeout(timeoutStep: FiniteDuration, timeoutMax: FiniteDuration) {

  /**
    * @param tryCnt number of failed tries
    * @return timeout value based on number of failed tries given as a parameter
    */
  def timeout(tryCnt: Int = 0): FiniteDuration = {
    val nextTimeout = timeoutStep * tryCnt
    if (timeoutMax > nextTimeout) nextTimeout else timeoutMax
  }

}

object ReconnectTimeout {

  def fromConfig(config: Config, timeoutStepKey: String = "step", timeoutMaxKey: String = "max"): ReconnectTimeout = {
    ReconnectTimeout(
      config.getDuration(timeoutStepKey),
      config.getDuration(timeoutMaxKey)
    )
  }

}
