package com.mysterria.lioqu.db.postgres.utils

import com.mysterria.lioqu.commons.db.SortOrder
import org.scalatest.flatspec.AnyFlatSpec
import thesis._

class SortOrderTest extends AnyFlatSpec {
  "SortOrder" should "be interpolated right" in {
    assertResult("asc")(SortOrder.Asc.toString)
    assertResult("desc")(SortOrder.Desc.toString)
  }

  "SortOrder" should "be parsed right" in {
    assertResult(SortOrder.Asc)(SortOrder.withName("asc"))
    assertResult(SortOrder.Desc)(SortOrder.withName("desc"))

    assertResult(SortOrder.Asc)(SortOrder.withNameIgnoreCase("aSc"))
    assertResult(SortOrder.Desc)(SortOrder.withNameIgnoreCase("DesC"))
  }
}
