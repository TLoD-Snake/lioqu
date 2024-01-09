package com.mysterria.lioqu.commons.net.conncontainer

import com.mysterria.lioqu.commons.net.conncontainer.ReconnectTimeout
import com.typesafe.config.ConfigFactory
import org.scalatest.flatspec.AnyFlatSpec

import scala.jdk.CollectionConverters._
import scala.concurrent.duration._

class ReconnectTimeoutTest extends AnyFlatSpec {

  "Reconnect timeout" should "return appropriate timeouts" in  {
    val step = 1.second
    val max = 3.second
    val rt0 = ReconnectTimeout(step, max)

    for (i <- 0 to 5) {
      assertResult(if (i.second < max) i.second else max){
        rt0.timeout(i)
      }
    }
  }

  it should "be created with right values from config" in {
    val config = ConfigFactory.parseMap(Map(
      "step" -> "10 second",
      "max" -> "20 second"
    ).asJava)

    val rt = ReconnectTimeout.fromConfig(config)
    assertResult(10.second){rt.timeoutStep}
    assertResult(20.second){rt.timeoutMax}
  }

  it should "throw exception when created from invalid config" in {
    assertThrows[com.typesafe.config.ConfigException.Missing] {
      ReconnectTimeout.fromConfig(ConfigFactory.empty())
    }

    assertThrows[com.typesafe.config.ConfigException.BadValue] {
      ReconnectTimeout.fromConfig(ConfigFactory.parseMap(Map(
        "step" -> "bla-bla?",
        "max" -> "BANANA!!!! =)"
      ).asJava))
    }
  }

}

