package com.mysterria.lioqu.commons.async

import com.mysterria.lioqu.commons.async.NamedThreadFactory

import java.util.{MissingFormatArgumentException, UnknownFormatConversionException}
import org.scalatest.flatspec.AnyFlatSpec

class NamedThreadFactoryTest extends AnyFlatSpec {
  classOf[NamedThreadFactory].getSimpleName should "create threads with desired names" in {
    val format = s"oifsffet-%d-zzzz"
    val tf = new NamedThreadFactory(format)
    for (i <- 1 to 10) {
      val t = tf.newThread(() => ())
      assertResult(format.replace("%d", i.toString))(t.getName)
    }
  }

  it should "fail fast on incorrect format" in {
    assertThrows[UnknownFormatConversionException] {
      new NamedThreadFactory("%zzz%")
    }
    assertThrows[NullPointerException] {
      new NamedThreadFactory(null)
    }
    assertThrows[MissingFormatArgumentException] {
      new NamedThreadFactory("%d%d")
    }
  }
}
