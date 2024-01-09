package com.mysterria.lioqu.commons

import org.scalatest.flatspec.AnyFlatSpec
import thesis._

class EnumerationExtensionsTest extends AnyFlatSpec {

  "Enum value" should "be found despite its case" in {
    assertResult(Nums.One)(Nums.withNameIgnoreCase("onE"))
    assertThrows[NoSuchElementException](Nums.withNameIgnoreCase("Elllleven"))
  }

  object Nums extends Enumeration {
    val One = Value(1)
  }
}
