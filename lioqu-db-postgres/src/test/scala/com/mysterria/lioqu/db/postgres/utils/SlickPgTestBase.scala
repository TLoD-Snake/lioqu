package com.mysterria.lioqu.db.postgres.utils

import com.mysterria.lioqu.db.postgres.utils.SlickPgDriver.api._
import com.mysterria.lioqu.db.postgres.utils.SlickPgDriver.backend.DatabaseDef
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.flatspec.AnyFlatSpec
import slick.dbio.{DBIOAction, NoStream}
import thesis._

import java.util.Properties
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Using

/**
  * sbt "test-only * -- -Dtest.db.url=jdbc:postgresql://localhost:5432/lioqu-test-db?user=lioqu-test-user&pass=secret"
  * sbt -Dtest.db.skip=false -Dtest.db.url="jdbc:postgresql://localhost:5432/lioqu-test-db?user=lioqu-test-user&pass=secret" test
  */
trait SlickPgTestBase extends AnyFlatSpec with LazyLogging {

  lazy val dbUrl: Option[String] = ifDef(!sys.props.get(SlickPgTestBase.SkipDbTestsKey).exists(_ equalsIgnoreCase "true")) {
    sys.props.get(SlickPgTestBase.DbConfigKey) getOrElse {
      val prop = new Properties()
      prop.load(getClass.getClassLoader.getResourceAsStream("testdb.properties"))
      prop.getProperty("db.url")
    }
  }

  implicit class DbRef[T](db: DatabaseDef) {
    /**
      * Same as db.run but awaits for result and returns it
      */
    @inline final def runRes[R](a: DBIOAction[R, NoStream, Nothing]): R = {
      val fut = db.run(a)
      Await.result(fut, 1.minute)
    }
  }

  //perhaps make db a field for shorter code
  /**
    * Takes care of db, will all f passing db
    */
  def slickTest(f: DatabaseDef => Any): Unit = {
    assume(dbUrl.isDefined)
    //managed(Database.forURL(dbUrl.get, driver = SlickPgTestBase.DbDriver)) acquireAndGet f
    Using.resource(Database.forURL(dbUrl.get, driver = SlickPgTestBase.DbDriver))(f)
  }

  /**
    * Takes care or table creation and dropping
    * Will call f passing db and table name
    */
  def singleTableTest(tableName: String, columns: String)(f: (DatabaseDef, String) => Any): Unit = slickTest { db =>
    var completed = false
    var dropped = false
    try {
      db.runRes(sqlu"""drop table if exists #$tableName""")
      db.runRes(sqlu"""create table #$tableName(#$columns)""")
      f(db, tableName)
      completed = true
    } finally {
      db.runRes(sqlu"""drop table #$tableName""")
      dropped = true
    }
    assertResult(completed && dropped)(true)
  }
}

object SlickPgTestBase {
  val DbDriver = "org.postgresql.Driver"
  val DbConfigKey = "test.db.url"
  val SkipDbTestsKey = "test.db.skip"
}