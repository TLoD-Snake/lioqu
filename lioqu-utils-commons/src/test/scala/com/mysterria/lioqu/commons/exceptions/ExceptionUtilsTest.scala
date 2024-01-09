package com.mysterria.lioqu.commons.exceptions

import com.mysterria.lioqu.commons.exceptions.{ConnectTimeoutException, ConnectivityException, ExceptionUtils}
import org.scalatest.flatspec.AnyFlatSpec

class ExceptionUtilsTest extends AnyFlatSpec {
  private def makeException = {
    new RuntimeException(
      new Exception(
        new UnsupportedOperationException(
          new NullPointerException("blah")
        )
      )
    )
  }

  "Wrapped exception" should "be unwrapped" in {
    val e = new RuntimeException(
      new Exception(
        new UnsupportedOperationException(
          new ConnectTimeoutException("blah")
        )
      )
    )

    val r = ExceptionUtils.unwrapToType[ConnectivityException](e)
    assert(r.isDefined)
    assert(r.get.isInstanceOf[ConnectivityException])
  }

  "Wrapped exception" should "not be unwrapped" in {
    val e = new RuntimeException(
      new Exception(
        new UnsupportedOperationException(
          new NullPointerException("blah")
        )
      )
    )

    val r = ExceptionUtils.unwrapToType[ConnectivityException](e)
    assert(r.isEmpty)
  }

  "Exception unwrapping" should "work as Throwable extension" in {
    import com.mysterria.lioqu.commons.exceptions.ExceptionUtils.ExceptionExtensions
    assert(makeException.unwrapToType[UnsupportedOperationException].isDefined)
  }
}
