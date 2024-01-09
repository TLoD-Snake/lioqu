package com.mysterria.lioqu.db.postgres.utils

import com.mysterria.lioqu.db.postgres.utils.SlickPgDriver.api._
import org.joda.time.DateTime
import slick.jdbc.GetResult

import java.sql.Timestamp
import java.util.{GregorianCalendar, TimeZone, UUID}

class PlainImplicitsTest extends SlickPgTestBase {

  "db" should "work" in slickTest { db =>
    assert(db != null)
  }

  "table" should "be available" in singleTableTest("TableForTest", "id integer") { (db, table) =>
    db.runRes(sqlu"""insert into #$table(id) values(1) """)
    db.runRes(sqlu"""insert into #$table(id) values(10) """)
    val res = db.runRes(sql"""select count(*) from #$table """.as[Int])
    assertResult(Seq(2))(res)
  }

  // if null is saved as arr column value (not some element, but entire column value is null) then empty Seq is retrieved. I think it's ok
  def nullToEmpty[T](a:Seq[T]):Seq[T] = if (a == null) List.empty else a

  "crud" should "work with integer[] with options" in singleTableTest("TableWithIntArr", "id integer, arr integer[]") { (db, table) =>
    val arrs = Array(List(Some(1), None, Some(3)),
      List(Some(4), None, Some(6)),
      Seq(Some(7), None, Some(9)),
      List(Some(10), Some(11), Some(12), Some(13), None, None),
      null, List(),List(Some(100)),List(None))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithIntArr(id: Int, arr: Seq[Option[Int]])
    implicit val getRowWithIntArr = GetResult(r => RowWithIntArr(r.nextInt(), r.nextOptionArray[Int]))
    val rows: Seq[RowWithIntArr] = db.runRes(sql"""select * from #$table""".as[RowWithIntArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }

  "crud" should "work with integer[] without options" in singleTableTest("TableWithIntArrNoNull", "id integer, arr integer[]") { (db, table) =>
    val arrs = Array(List(1,3),
      List(4,6),
      Seq(7,9),
      List(10,11,12,13),null,List(),List(100))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
  val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
  val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithIntArr(id: Int, arr: Seq[Int])
    implicit val getRowWithIntArr = GetResult(r => RowWithIntArr(r.nextInt(), r.nextArray[Int]()))
    val rows: Seq[RowWithIntArr] = db.runRes(sql"""select * from #$table""".as[RowWithIntArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }


  "crud" should "work with long[] with options" in singleTableTest("TableWithLongArr", "id integer, arr int8[]") { (db, table) =>
    val arrs = Array(List(Some(1.toLong), None, Some(3.toLong)),
      List(Some(4.toLong), None, Some(6.toLong)),
      Seq(Some(7.toLong), None, Some(9.toLong)),
      List(Some(10.toLong), Some(11.toLong), Some(12.toLong), Some(13.toLong), None, None),
      null, List(), List(Some(100.toLong)), List(None))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithLongArr(id: Int, arr: Seq[Option[Long]])
    implicit val getRowWithLongArr = GetResult(r => RowWithLongArr(r.nextInt(), r.nextOptionArray[Long]))
    val rows: Seq[RowWithLongArr] = db.runRes(sql"""select * from #$table""".as[RowWithLongArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }

  "crud" should "work with long[] without options" in singleTableTest("TableWithLongArrNoNull", "id integer, arr int8[]") { (db, table) =>
    val arrs = Array(List(1.toLong, 3.toLong),
      List(4.toLong, 6.toLong),
      Seq(7.toLong, 9.toLong),
      List(10.toLong, 11.toLong, 12.toLong, 13.toLong),
      null,List(),List(100.toLong))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithLongArr(id: Int, arr: Seq[Long])
    implicit val getRowWithLongArr = GetResult(r => RowWithLongArr(r.nextInt(), r.nextArray[Long]()))
    val rows: Seq[RowWithLongArr] = db.runRes(sql"""select * from #$table""".as[RowWithLongArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }

  "crud" should "work with short[] with options" in singleTableTest("TableWithShortArr", "id integer, arr int2[]") { (db, table) =>
    val arrs = Array(List(Some(1.toShort), None, Some(3.toShort)),
      List(Some(4.toShort), None, Some(6.toShort)),
      Seq(Some(7.toShort), None, Some(9.toShort)),
      List(Some(10.toShort), Some(11.toShort), Some(12.toShort), Some(13.toShort), None, None),
      null,List(), List(Some(100.toShort)), List(None))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithShortArr(id: Int, arr: Seq[Option[Short]])
    implicit val getRowWithShortArr = GetResult(r => RowWithShortArr(r.nextInt(), r.nextOptionArray[Short]))
    val rows: Seq[RowWithShortArr] = db.runRes(sql"""select * from #$table""".as[RowWithShortArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }

  "crud" should "work with short[] without options" in singleTableTest("TableWithShortArrNoNull", "id integer, arr int2[]") { (db, table) =>
    val arrs = Array(List(1.toShort, 3.toShort),
      List(4.toShort, 6.toShort),
      Seq(7.toShort, 9.toShort),
      List(10.toShort, 11.toShort, 12.toShort, 13.toShort),
        null,List(),List(100.toShort))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithShortArr(id: Int, arr: Seq[Short])
    implicit val getRowWithShortArr = GetResult(r => RowWithShortArr(r.nextInt(), r.nextArray[Short]()))
    val rows: Seq[RowWithShortArr] = db.runRes(sql"""select * from #$table""".as[RowWithShortArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }

  "crud" should "work with float[] with options" in singleTableTest("TableWithFloatArr", "id integer, arr float4[]") { (db, table) =>
    val arrs = Array(List(Some(0.1.toFloat), None, Some(0.3.toFloat)),
      List(Some(0.4.toFloat), None, Some(0.6.toFloat)),
      Seq(Some(0.7.toFloat), None, Some(0.9.toFloat)),
      List(Some(0.01.toFloat), Some(0.11.toFloat), Some(0.12.toFloat), Some(0.13.toFloat), None, None),
      null, List(), List(Some(100.toFloat)), List(None))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithFloatArr(id: Int, arr: Seq[Option[Float]])
    implicit val getRowWithFloatArr = GetResult(r => RowWithFloatArr(r.nextInt(), r.nextOptionArray[Float]))
    val rows: Seq[RowWithFloatArr] = db.runRes(sql"""select * from #$table""".as[RowWithFloatArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }

  "crud" should "work with float[] without options" in singleTableTest("TableWithFloatArrNoNull", "id integer, arr float4[]") { (db, table) =>
    val arrs = Array(List(0.1.toFloat, 0.3.toFloat),
      List(0.4.toFloat, 0.6.toFloat),
      Seq(0.7.toFloat, 0.9.toFloat),
      List(0.01.toFloat, 0.11.toFloat, 0.12.toFloat, 0.13.toFloat),
      null, List(), List(100.toFloat))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithFloatArr(id: Int, arr: Seq[Float])
    implicit val getRowWithFloatArr = GetResult(r => RowWithFloatArr(r.nextInt(), r.nextArray[Float]()))
    val rows: Seq[RowWithFloatArr] = db.runRes(sql"""select * from #$table""".as[RowWithFloatArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }

  "crud" should "work with double[] with options" in singleTableTest("TableWithDoubleArr", "id integer, arr float8[]") { (db, table) =>
    val arrs = Array(List(Some(0.1.toDouble), None, Some(0.3.toDouble)),
      List(Some(0.4.toDouble), None, Some(0.6.toDouble)),
      Seq(Some(0.7.toDouble), None, Some(0.9.toDouble)),
      List(Some(0.01.toDouble), Some(0.11.toDouble), Some(0.12.toDouble), Some(0.13.toDouble), None, None),
      null, List(), List(Some(100.toDouble)), List(None))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithDoubleArr(id: Int, arr: Seq[Option[Double]])
    implicit val getRowWithDoubleArr = GetResult(r => RowWithDoubleArr(r.nextInt(), r.nextOptionArray[Double]))
    val rows: Seq[RowWithDoubleArr] = db.runRes(sql"""select * from #$table""".as[RowWithDoubleArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }

  "crud" should "work with double[] without options" in singleTableTest("TableWithDoubleArrNoNull", "id integer, arr float8[]") { (db, table) =>
    val arrs = Array(List(0.1.toDouble, 0.3.toDouble),
      List(0.4.toDouble, 0.6.toDouble),
      Seq(0.7.toDouble, 0.9.toDouble),
      List(0.01.toDouble, 0.11.toDouble, 0.12.toDouble, 0.13.toDouble),
      null, List(), List(100.toDouble))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithDoubleArr(id: Int, arr: Seq[Double])
    implicit val getRowWithDoubleArr = GetResult(r => RowWithDoubleArr(r.nextInt(), r.nextArray[Double]()))
    val rows: Seq[RowWithDoubleArr] = db.runRes(sql"""select * from #$table""".as[RowWithDoubleArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }



  "crud" should "work with boolean[] with options" in singleTableTest("TableWithBoolArr", "id integer, arr boolean[]") { (db, table) =>
    val arrs = Array(List(Some(true), None, Some(false)),
      List(Some(false), None, Some(true)),
      Seq(Some(false), None, Some(false)),
      List(Some(true), Some(true), Some(true), Some(false), None, None),
      null, List(), List(Some(true)), List(None))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithBoolArr(id: Int, arr: Seq[Option[Boolean]])
    implicit val getRowWithBoolArr = GetResult(r => RowWithBoolArr(r.nextInt(), r.nextOptionArray[Boolean]))
    val rows: Seq[RowWithBoolArr] = db.runRes(sql"""select * from #$table""".as[RowWithBoolArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }

  "crud" should "work with boolean[] without options" in singleTableTest("TableWithBoolArrNoNull", "id integer, arr boolean[]") { (db, table) =>
    val arrs = Array(List(true, false),
      List(false, true),
      Seq(false, false),
      List(true, true, true, false),
      null,List(),List(true))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithBoolArr(id: Int, arr: Seq[Boolean])
    implicit val getRowWithBoolArr = GetResult(r => RowWithBoolArr(r.nextInt(), r.nextArray[Boolean]()))
    val rows: Seq[RowWithBoolArr] = db.runRes(sql"""select * from #$table""".as[RowWithBoolArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }

  "crud" should "work with uuid[] with options" in singleTableTest("TableWithUuidArr", "id integer, arr uuid[]") { (db, table) =>
    val arrs = Array(List(Some(UUID.randomUUID()), None, Some(UUID.randomUUID())),
      List(Some(UUID.randomUUID()), None, Some(UUID.randomUUID())),
      Seq(Some(UUID.randomUUID()), None, Some(UUID.randomUUID())),
      List(Some(UUID.randomUUID()), Some(UUID.randomUUID()), Some(UUID.randomUUID()), Some(UUID.randomUUID()), None, None),
      null,List(),List(Some(UUID.randomUUID())),List(None))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithUuidArr(id: Int, arr: Seq[Option[UUID]])
    implicit val getRowWithUuidArr = GetResult(r => RowWithUuidArr(r.nextInt(), r.nextOptionArray[UUID]))
    val rows: Seq[RowWithUuidArr] = db.runRes(sql"""select * from #$table""".as[RowWithUuidArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }

  "crud" should "work with uuid[] without options" in singleTableTest("TableWithUuidArrNoNull", "id integer, arr uuid[]") { (db, table) =>
    val arrs = Array(List(UUID.randomUUID(), UUID.randomUUID()),
      List(UUID.randomUUID(), UUID.randomUUID()),
      Seq(UUID.randomUUID(), UUID.randomUUID()),
      List(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
      null, List(), List(UUID.randomUUID()))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithUuidArr(id: Int, arr: Seq[UUID])
    implicit val getRowWithUuidArr = GetResult(r => RowWithUuidArr(r.nextInt(), r.nextArray[UUID]()))
    val rows: Seq[RowWithUuidArr] = db.runRes(sql"""select * from #$table""".as[RowWithUuidArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }

  "crud" should "work with text[] with options" in singleTableTest("TableWithTextArr", "id integer, arr text[]") { (db, table) =>
    val arrs = Array(List(Some("a"), None, Some("b")),
      List(Some("c"), None, Some("d")),
      Seq(Some("e"), None, Some("f")),
      List(Some("h"), Some("i"), Some("j"), Some("k"), None, None),
      null,List(),List(Some("z")),List(None))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithTextArr(id: Int, arr: Seq[Option[String]])
    implicit val getRowWithTextArr = GetResult(r => RowWithTextArr(r.nextInt(), r.nextOptionArray[String]))
    val rows: Seq[RowWithTextArr] = db.runRes(sql"""select * from #$table""".as[RowWithTextArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }


  "crud" should "work with text[] without options" in singleTableTest("TableWithTextArrNoNull", "id integer, arr text[]") { (db, table) =>
    val arrs = Array(List("a", "b"),
      List("c", "d"),
      Seq("e", "f"),
      List("h", "i", "j", "k"),
      null,List(),List("z"))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithTextArr(id: Int, arr: Seq[String])
    implicit val getRowWithTextArr = GetResult(r => RowWithTextArr(r.nextInt(), r.nextArray[String]()))
    val rows: Seq[RowWithTextArr] = db.runRes(sql"""select * from #$table""".as[RowWithTextArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }


  import java.sql.Date


  def mkDate(y:Int, m:Int, d:Int) = {
    val cal = new GregorianCalendar(TimeZone.getDefault())
    cal.set(y,m-1,d)
    new Date(cal.getTimeInMillis)
  }

  "crud" should "work with date[] with options" in singleTableTest("TableWithDateArr", "id integer, arr date[]") { (db, table) =>
    val arrs = Array(List(Some(mkDate(1925,12,25)), None, Some(mkDate(1925,12,26))),
      List(Some(mkDate(1925,12,27)), None, Some(mkDate(1925,12,28))),
      Seq(Some(mkDate(1925,12,29)), None, Some(mkDate(1925,12,30))),
      List(Some(mkDate(1925,12,31)), Some(mkDate(1926,1,1)), Some(mkDate(1926,1,2)), Some(mkDate(1926,1,3)), None, None),
      null,List(),List(Some(mkDate(1900,1,1))), List(None))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithDateArr(id: Int, arr: Seq[Option[Date]])
    implicit val getRowWithDateArr = GetResult(r => RowWithDateArr(r.nextInt(), r.nextOptionArray[Date]))
    val rows: Seq[RowWithDateArr] = db.runRes(sql"""select * from #$table""".as[RowWithDateArr])

    assertResult(arrs.length - 2)(rows.length)
    // smaller than date portion of date gets truncated in DB, so retrieved Date is unequal to original
    // even thought they represent same ymd, so I compare .toStrings,  these will be "Some(1925-12-25)" or "None"
    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)).map(_.toString))(rows(i).arr.map(_.toString))
    }
  }

  "crud" should "work with date[] without options" in singleTableTest("TableWithDateArrNoNull", "id integer, arr date[]") { (db, table) =>
    val arrs = Array(List(mkDate(1925,12,25), mkDate(1925,12,26)),
      List(mkDate(1925,12,27), mkDate(1925,12,28)),
      Seq(mkDate(1925,12,29), mkDate(1925,12,30)),
      List(mkDate(1925,12,31), mkDate(1926,1,1), mkDate(1926,1,2), mkDate(1926,1,3)),
      null, List(), List(mkDate(2000,2,2)))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithDateArr(id: Int, arr: Seq[Date])
    implicit val getRowWithDateArr = GetResult(r => RowWithDateArr(r.nextInt(), r.nextArray[Date]()))
    val rows: Seq[RowWithDateArr] = db.runRes(sql"""select * from #$table""".as[RowWithDateArr])

    assertResult(arrs.length - 2)(rows.length)
    // smaller than date portion of date gets truncated in DB, so retrieved Date is unequal to original
    // even thought they represent same ymd, so I compare .toStrings,  these will be "Some(1925-12-25)" or "None"
    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)).map(_.toString))(rows(i).arr.map(_.toString))
    }
  }



  import java.sql.Time

  def mkTime(h:Int, m:Int, s:Int) = {
    val cal = new GregorianCalendar(TimeZone.getDefault())
    cal.set(0,0,0,h,m,s)
    new Time(cal.getTimeInMillis)
  }

  "crud" should "work with time[] with options" in singleTableTest("TableWithTimeArr", "id integer, arr time[]") { (db, table) =>
    val arrs = Array(List(Some(mkTime(1,1,1)), None, Some(mkTime(1,1,2))),
      List(Some(mkTime(1,1,3)), None, Some(mkTime(1,1,4))),
      Seq(Some(mkTime(1,1,5)), None, Some(mkTime(1,1,6))),
      List(Some(mkTime(1,1,7)), Some(mkTime(1,1,8)), Some(mkTime(1,1,9)), Some(mkTime(1,1,10)), None, None),
      null,List(),List(Some(mkTime(3,3,3))),List(None))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithTimeArr(id: Int, arr: Seq[Option[Time]])
    implicit val getRowWithTimeArr = GetResult(r => RowWithTimeArr(r.nextInt(), r.nextOptionArray[Time]))
    val rows: Seq[RowWithTimeArr] = db.runRes(sql"""select * from #$table""".as[RowWithTimeArr])

    assertResult(arrs.length - 2)(rows.length)
    // objects I retrieved represent same time as original but their .getTime are different from originals
    // so I compare .toStrings, these will be "Some(01:01:07)" and "None"
    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)).map(_.toString))(rows(i).arr.map(_.toString))
    }
  }

  "crud" should "work with time[] without options" in singleTableTest("TableWithTimeArrNoNull", "id integer, arr time[]") { (db, table) =>
    val arrs = Array(List(mkTime(1,1,1), mkTime(1,1,2)),
      List(mkTime(1,1,3), mkTime(1,1,4)),
      Seq(mkTime(1,1,5), mkTime(1,1,6)),
      List(mkTime(1,1,7), mkTime(1,1,8), mkTime(1,1,9), mkTime(1,1,10)),
      null,List(),List(mkTime(5,5,5)))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithTimeArr(id: Int, arr: Seq[Time])
    implicit val getRowWithTimeArr = GetResult(r => RowWithTimeArr(r.nextInt(), r.nextArray[Time]()))
    val rows: Seq[RowWithTimeArr] = db.runRes(sql"""select * from #$table""".as[RowWithTimeArr])
    assertResult(arrs.length - 2)(rows.length)
    // objects I retrieved represent same time as original but their .getTime are different from originals
    // so I compare .toStrings, these will be "Some(01:01:07)" and "None"
    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)).map(_.toString))(rows(i).arr.map(_.toString))
    }
  }

  var time = System.currentTimeMillis()
  def mkTimestamp = {
    time += 5000
    new Timestamp(time)
  }

  "crud" should "work with timestamp[] with options" in singleTableTest("TableWithTimestampArr", "id integer, arr timestamp[]") { (db, table) =>
    val arrs = Array(List(Some(mkTimestamp), None, Some(mkTimestamp)),
      List(Some(mkTimestamp), None, Some(mkTimestamp)),
      Seq(Some(mkTimestamp), None, Some(mkTimestamp)),
      List(Some(mkTimestamp), Some(mkTimestamp), Some(mkTimestamp), Some(mkTimestamp), None, None),
      null,List(),List(Some(mkTimestamp)),List(None))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithTimestampArr(id: Int, arr: Seq[Option[Timestamp]])
    implicit val getRowWithTimestampArr = GetResult(r => RowWithTimestampArr(r.nextInt(), r.nextOptionArray[Timestamp]))
    val rows: Seq[RowWithTimestampArr] = db.runRes(sql"""select * from #$table""".as[RowWithTimestampArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }

  "crud" should "work with timestamp[] without options" in singleTableTest("TableWithTimestampArroNull", "id integer, arr timestamp[]") { (db, table) =>
    val arrs = Array(List(mkTimestamp, mkTimestamp),
      List(mkTimestamp, mkTimestamp),
      Seq(mkTimestamp, mkTimestamp),
      List(mkTimestamp, mkTimestamp, mkTimestamp, mkTimestamp),
      null,List(),List(mkTimestamp))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithTimestampArr(id: Int, arr: Seq[Timestamp])
    implicit val getRowWithTimestampArr = GetResult(r => RowWithTimestampArr(r.nextInt(), r.nextArray[Timestamp]()))
    val rows: Seq[RowWithTimestampArr] = db.runRes(sql"""select * from #$table""".as[RowWithTimestampArr])
    assertResult(arrs.length - 2)(rows.length)

    for(i<- 0 until rows.length) {
      assertResult(i+2)(rows(i).id)
      assertResult(nullToEmpty(arrs(i+2)))(rows(i).arr)
    }
  }


  var t = System.currentTimeMillis()
  def mkDateTime = {
    t += 10000
    new DateTime(t)
  }

  "crud" should "work with timestamptz[]" in singleTableTest("TableWithTimestamptzArr", "id integer, arr timestamptz[]") { (db, table) =>
    val arrs = Array(List(Some(mkDateTime), None, Some(mkDateTime)),
      List(Some(mkDateTime), None, Some(mkDateTime)),
      Seq(Some(mkDateTime), None, Some(mkDateTime)),
      List(Some(mkDateTime), Some(mkDateTime), Some(mkDateTime), Some(mkDateTime), None, None))
    val insertCommands = (for (i <- 0 until arrs.length)
      yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))

    db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

    val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
    assertResult(arrs.length)(totalCount.head)

    db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
    val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(2)(count1.head)

    db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
    val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
    assertResult(0)(count2.head)

    case class RowWithTimestamptzArr(id: Int, arr: Seq[Option[DateTime]])

    val rows = db.runRes(sql"""select id, arr from #$table;""".as[(Int, Seq[Option[DateTime]])]).map(r => RowWithTimestamptzArr(r._1, r._2))
    assertResult(2)(rows.length)
    assertResult(2)(rows(0).id)

    val originalValues = arrs(3).map(_.map(_.getMillis))
    val dbValues = rows(1).arr.map(_.map(_.getMillis))
    assertResult(originalValues)(dbValues)
  }

  /*

  an attempt to make generic test

  def testPlainImplicits[T](gen: () => T, sqlType: String, compMap: (Option[T] => Any)) = {
    val arrs :Array[Seq[Option[T]]] = Array(Seq(Some(gen()), None, Some(gen())),
      Seq(Some(gen()), None, Some(gen())),
      Seq(None, Some(gen()), None),
      Seq(Some(gen()), Some(gen()), Some(gen()), Some(gen()), None, None))

    singleTableTest(s"TableWith${sqlType}Array", s"id integer, arr ${sqlType}[]"){(db,table)=>
      val insertCommands = (for (i <- 0 until arrs.length)
        yield (sqlu"""insert into #$table(id, arr) values ($i, ${arrs(i)})"""))
      db.runRes(DBIO.seq(insertCommands: _*)) // multiple inserts

      val totalCount = db.runRes(sql"""select count(*) from #$table """.as[Int]) // check count of inserted
      assertResult(arrs.length)(totalCount.head)

      db.runRes(sqlu"""update #$table set arr=${arrs(1)} where id=0 """) // update
      val count1 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
      assertResult(2)(count1.head)

      db.runRes(sqlu"""delete from #$table where arr=${arrs(1)}""") // delete
      val count2 = db.runRes(sql"""select count(*) from #$table where arr = ${arrs(1)}""".as[Int])
      assertResult(0)(count2.head)

      case class RowWithArr(id: Int, arr: Seq[Option[T]])
      implicit val getRowWithArr = GetResult(r => RowWithArr(r.nextInt, r.nextOptionArray[T]))
      val rows: Seq[RowWithArr] = db.runRes(sql"""select * from #$table""".as[RowWithArr])
      assertResult(2)(rows.length)
      assertResult(2)(rows(0).id)

      assertResult(arrs(3).map(compMap(_)))(rows(1).arr.map(compMap(_)))
    }
    //this.s


  }*/

}
