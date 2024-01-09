package com.mysterria.lioqu.commons

import org.scalatest.Succeeded
import org.scalatest.flatspec.AnyFlatSpec
import thesis._

import java.time.{Duration => JDuration}
import scala.concurrent.duration.Duration

class ThesisTest extends AnyFlatSpec {
  "Double" should "be rounded with defined precision" in {
    assertResult(10.11)(10.105.roundAt(2))
    assertResult(10.11)(10.111.roundAt(2))

    assertResult(10.01234567890)(10.0123456789012.roundAt(11))
    assertResult(10.01234567891)(10.0123456789052.roundAt(11))
  }

  "MapExtensions" should "work" in {
    val min = 0
    val max = 10

    val map = min until max map { n => n -> s"value of $n"} toMap

    for (i <- min until max) {
      val message = s"Map should containt value for key $i"
      map.getOrThrow(i, message)
      map.getOrThrowEx(i, new Exception(message))
      map.getOrThrowRef[RuntimeException](i, message)
    }

    val maxplus1 = max + 1
    val message = s"Map doesn't contain value for key $maxplus1"
    assertThrows[Throwable] {
      map.getOrThrow(maxplus1, message)
    }
    assertThrows[Throwable] {
      map.getOrThrowEx(maxplus1, new Exception(message))
    }
    assertThrows[IllegalArgumentException] {
      map.getOrThrowRef[IllegalArgumentException](maxplus1, message)
    }
  }

  "Cast Java Duration to Scala Duration" should "work implicitly when thesis is imported" in {
    val jd: JDuration = JDuration.ZERO
    def pd(d: Duration): String = s"Scala duration is $d"
    pd(jd)
    Succeeded
  }
}
