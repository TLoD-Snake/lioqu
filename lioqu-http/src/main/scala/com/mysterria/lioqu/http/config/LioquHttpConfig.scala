package com.mysterria.lioqu.http.config

import com.typesafe.config.Config
import com.mysterria.lioqu.config._
import com.mysterria.lioqu.tools.LioquConfigHelper

case class LioquHttpConfig(
  bindHost: String,
  bindPort: Int,
  wsOutBuffer: Int
)

object LioquHttpConfig {
  val HttpConfigPath = s"${Lioqu}.http"
  val DefaultWsOutBuffer = 1024

  case object ConfigKeys {
    val BindHost = "bind-host"
    val BindPort = "bind-port"
    val WsOutBuffer = "ws-out-buffer"
  }

  def apply(config: Config): LioquHttpConfig = {
    import ConfigKeys._
    val httpConfig = config.getConfig(HttpConfigPath)
    val helper = LioquConfigHelper(httpConfig)
    new LioquHttpConfig(
      bindHost = httpConfig.getString(BindHost),
      bindPort = httpConfig.getInt(BindPort),
      wsOutBuffer = helper.withDefault(WsOutBuffer, _.getInt, DefaultWsOutBuffer)
    )
  }
}
