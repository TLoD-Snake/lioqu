package com.mysterria.lioqu.tools

import javax.swing.filechooser.FileNameExtensionFilter

import scala.concurrent.duration._
import scala.util.Random
import scala.math._

object DelayTools {

  /**
    * @return random delay between min and max.
    */
  def delay(min: FiniteDuration, max: FiniteDuration): FiniteDuration = {
    val diff = abs(max.toMillis - min.toMillis)
    (Seq(min, max).min.toMillis + BigDecimal(diff * Random.nextDouble).setScale(0, BigDecimal.RoundingMode.HALF_UP).toLong).millis
  }

}
