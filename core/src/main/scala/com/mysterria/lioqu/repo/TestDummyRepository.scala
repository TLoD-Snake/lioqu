package com.mysterria.lioqu.repo

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait TestDummyRepository extends TestDummyTable { this: DefaultDBComponenet =>
  protected implicit val ec: ExecutionContext
  import profile.api._


  def ddl = db.run {query.schema.create}
}

trait TestDummyTable { this: DefaultDBComponenet =>
  import profile.api._

  class TestDummyTable(tag: Tag) extends Table[TestDummy](tag, schema, "lioqu_test_dummy") {
    val id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val name = column[String]("name", O.Unique)
    def * = (name, id.?) <> (TestDummy.tupled, TestDummy.unapply)
  }

  protected val query = TableQuery[TestDummyTable]
}

class TestDummyRepositoryImpl @Inject()(implicit protected val ec: ExecutionContext) extends TestDummyRepository with DefaultDBComponenet


case class TestDummy(name: String, id: Option[Int] = None)
