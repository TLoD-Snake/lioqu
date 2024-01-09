package com.mysterria.lioqu.db.postgres.utils

import org.scalatest.flatspec.AnyFlatSpec

class DbServiceBaseTest extends AnyFlatSpec {
  s"Auxiliary ${classOf[DbServiceBase].getSimpleName} methods names" should "be detected properly" in {
    assert(DbServiceBase.utilMethodNames.contains("withPsqlHandling"))
    assert(DbServiceBase.utilMethodNames.contains("singleValueQuery"))
    assert(DbServiceBase.utilMethodNames.contains("voidQuery"))
  }
}
