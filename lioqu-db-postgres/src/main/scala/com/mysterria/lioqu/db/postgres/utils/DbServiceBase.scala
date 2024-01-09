package com.mysterria.lioqu.db.postgres.utils

import com.mysterria.lioqu.commons.TimeLoggingSettings
import com.mysterria.lioqu.db.utils.DbException
import com.typesafe.scalalogging.LazyLogging
import org.postgresql.util.PSQLException
import slick.jdbc.SQLActionBuilder
import thesis._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by gmany on 6/2/2016.
  */
abstract class DbServiceBase(dbProvider: ServiceDbProvider)(implicit val ec: ExecutionContext, val timeLoggingSettings: TimeLoggingSettings) extends LazyLogging {
  val db = dbProvider.dbConf.db

  // In this file you'll find only common utility method implementations.
  // Look into the `with` traits of this class to see traits which actually implement DB access.

  def withPsqlHandling[T](action: => Future[T]): Future[T] = {
    val start = System.currentTimeMillis()
    val callStack: Array[StackTraceElement] = new Throwable().getStackTrace // this takes 2 microseconds

    val callerOption = callStack.iterator.dropWhile(e => DbServiceBase.utilMethodNames.contains(e.getMethodName)).nextOption()

    val result = action mapAll {
      case Success(res) => res
      case Failure(err) => err match {
        case err: PSQLException => throw PgDetailedException(err, callStack)
        case err: Throwable => throw DbException(err, callStack)
      }
    }

    callerOption.foreach { caller =>
      result.foreach { _ =>
        __log_time__(start, s"DBTIME: $caller")(logger, timeLoggingSettings)
      }
    }

    result
  }

  def singleValueQuery[T](sql: => SQLActionBuilder)
                         (implicit rconv: slick.jdbc.GetResult[T]): Future[Option[T]] = withPsqlHandling {
    val query = sql.as[(T)]

    db.run(query.headOption)
  }

  def voidQuery(sql: => SQLActionBuilder)
               (implicit rconv: slick.jdbc.GetResult[Long]): Future[Unit] =
    singleValueQuery[Long](sql) map (_ => ())
}

object DbServiceBase {
  import com.github.dwickern.macros.NameOf._

  val utilMethodNames = mutable.Set(
    nameOf[DbServiceBase](d => (action: Future[_]) => d.withPsqlHandling(action) ),
    nameOf[DbServiceBase](d => (sql: SQLActionBuilder, rconv: slick.jdbc.GetResult[_]) => d.singleValueQuery(sql)(rconv)),
    nameOf[DbServiceBase](d => d.voidQuery _)
  )

  def addUtilMethod[T](f: T => Any): Unit = utilMethodNames += nameOf[T](f)
}
