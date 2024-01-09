package com.mysterria.lioqu.db.postgres.utils

import com.github.tminglei.slickpg.PgArraySupport
import com.github.tminglei.slickpg.utils.PgTokenHelper._
import com.github.tminglei.slickpg.utils.PlainSQLUtils.mkGetResult
import com.github.tminglei.slickpg.utils.SimpleArrayUtils._
import com.typesafe.scalalogging.Logger
import com.mysterria.lioqu.commons.logging.LogHelpers._
import org.joda.time.{DateTime, DateTimeZone}
import org.postgresql.jdbc.PgArray
import org.slf4j.LoggerFactory
import slick.jdbc._

import java.sql.{Date, Time, Timestamp}
import java.util.UUID
import scala.collection.immutable.ArraySeq
import scala.reflect.ClassTag

trait PgJavaBoxArraySupport extends PgArraySupport {
  driver: PostgresProfile with FixedPgDateSupportJoda =>

  private lazy val logger = Logger(LoggerFactory.getLogger("com.swipestox.util.db.PgJavaBoxArraySupport"))

  object ArrayNull extends Chunk("NULL") {
    override def toString = value
  }

  trait OptionArrayPlainImplicits extends SimpleArrayPlainImplicits with FixedJodaDateTimeFormatters {
    // GET ----------
    implicit class PgOptionArrayPositionedResult(r: PositionedResult) extends PgArrayPositionedResult(r) {
      def nextOptionArray[T](): Seq[Option[T]] = nextOptionArrayOption[T]().getOrElse(Nil)
      def nextOptionArrayOption[T](): Option[Seq[Option[T]]] = {
        simpleNextOptionArray[T, T](r)(identity)
      }

      def nextOptionArrayMapped[A, B](mapper: A => B): Seq[Option[B]] = {
        nextOptionArrayOptionMapped[A, B](mapper).getOrElse(Nil)
      }
      def nextOptionArrayOptionMapped[A, B](mapper: A => B): Option[Seq[Option[B]]] = {
        simpleNextOptionArray[A, B](r)(mapper)
      }
    }

    private def simpleNextOptionArray[A, B](r: PositionedResult)(cv: A => B): Option[Seq[Option[B]]] = {
      val value = r.rs.getArray(r.skip.currentPos)
      if (r.rs.wasNull) {
        None
      } else {
        Some(
          ArraySeq.unsafeWrapArray(
            value.getArray.asInstanceOf[Array[Any]].map(x => Option(x.asInstanceOf[A]).map(cv))
          )
        )
      }
    }

    // SET ----------
    def mkArraySetParameter[T: ClassTag](baseType: String,
                                         toStr: (T => String) = (v: T) => v.toString,
                                         seqToStr: Option[(Seq[T] => String)] = None): SetParameter[Seq[T]] = {
      new SetParameter[Seq[T]] {
        def apply(v: Seq[T], pp: PositionedParameters) = internalSetArray(baseType, Option(v), pp, toStr, seqToStr)
      }
    }

    def mkArrayOptionSetParameter[T: ClassTag](baseType: String,
                                               toStr: (T => String) = (v: T) => v.toString,
                                               seqToStr: Option[(Seq[T] => String)] = None): SetParameter[Option[Seq[T]]] = {
      new SetParameter[Option[Seq[T]]] {
        def apply(v: Option[Seq[T]], pp: PositionedParameters) = internalSetArray(baseType, v, pp, toStr, seqToStr)
      }
    }

    private def internalSetArray[T: ClassTag](baseType: String,
                                              v: Option[Seq[T]],
                                              p: PositionedParameters,
                                              toStr: (T => String),
                                              seqToStr: Option[(Seq[T] => String)]): Unit = {
      internalSetOptionArray(baseType, v.map(_.map(x => Option(x))), p, toStr, seqToStr)
    }

    ///
    def mkArraySetOptionParameter[T: ClassTag](baseType: String,
                                               toStr: (T => String) = (v: T) => v.toString,
                                               seqToStr: Option[(Seq[T] => String)] = None): SetParameter[Seq[Option[T]]] =
      new SetParameter[Seq[Option[T]]] {
        def apply(v: Seq[Option[T]], pp: PositionedParameters) = internalSetOptionArray(baseType, Option(v), pp, toStr, seqToStr)
      }

    def mkArrayOptionSetOptionParameter[T: ClassTag](baseType: String,
                                                     toStr: (T => String) = (v: T) => v.toString,
                                                     seqToStr: Option[(Seq[T] => String)] = None): SetParameter[Option[Seq[Option[T]]]] =
      new SetParameter[Option[Seq[Option[T]]]] {
        def apply(v: Option[Seq[Option[T]]], pp: PositionedParameters) = internalSetOptionArray(baseType, v, pp, toStr, seqToStr)
      }

    private implicit class OptionOrNullEx[T](o: Option[T]) {
      @inline final def orNullEx: T = {
        val x: Any = o getOrElse ArrayNull

        x
      }.asInstanceOf[T]
    }

    def mkStringOpt[T](ToString: T => String)(value: Seq[T]): String =
      getString(value match {
        case null  => Null
        case vList =>
          val sq = scala.collection.mutable.ArrayBuffer[Token](Open("{"))

          var first = true
          for (v <- vList) {
            if (!first) {
              sq += Chunk(",")
            } else {
              first = false
            }

            sq += (if (v == null) {
              ArrayNull
            } else {
              Chunk(ToString(v))
            })
          }

          sq += Close("}")

          GroupToken(sq.toSeq)
      }, -1)

    private def internalSetOptionArray[T: ClassTag](
                                                     baseType: String,
                                                     v: Option[Seq[Option[T]]],
                                                     p: PositionedParameters,
                                                     toStr: (T => String),
                                                     seqToStr: Option[(Seq[T] => String)]
                                                   ): Unit = {
      val _seqToStr = seqToStr.getOrElse(mkStringOpt(toStr) _)

      p.pos += 1

      v match {
        case Some(vList) =>
          val sqlArray = mkArray(_seqToStr)(baseType, vList.map(_.orNullEx))

          logger.trace(log"Serialized array $v into $sqlArray")

          p.ps.setArray(p.pos, sqlArray)

        case None        => p.ps.setNull(p.pos, java.sql.Types.ARRAY)
      }
    }

    def supportNull[T](toStr: T => String)(v: AnyRef): String = v match {
      case ArrayNull => v.toString
      case s =>
        val b = new java.lang.StringBuilder()
        PgArray.escapeArrayElement(b, toStr(s.asInstanceOf[T]))

        b.toString
    }

    def quotedString[T](v: T): String = supportNull[T](_.toString)(v.asInstanceOf[AnyRef])

    //////////////////////////////////////////////////////////////////////////
    implicit val getUUIDOptionArray = mkGetResult(_.nextOptionArray[UUID]())
    implicit val getUUIDOptionArrayOption = mkGetResult(_.nextOptionArrayOption[UUID]())
    implicit val setUUIDOptionArray = mkArraySetOptionParameter[UUID]("uuid", toStr = quotedString)
    implicit val setUUIDOptionArrayOption = mkArrayOptionSetOptionParameter[UUID]("uuid", toStr = quotedString)
    ///
    implicit val getStringOptionArray = mkGetResult(_.nextOptionArray[String]())
    implicit val getStringOptionArrayOption = mkGetResult(_.nextOptionArrayOption[String]())
    implicit val setStringOptionArray = mkArraySetOptionParameter[String]("text", toStr = quotedString)
    implicit val setStringOptionArrayOption = mkArrayOptionSetOptionParameter[String]("text", toStr = quotedString)
    ///
    implicit val getLongOptionArray = mkGetResult(_.nextOptionArray[Long]())
    implicit val getLongOptionArrayOption = mkGetResult(_.nextOptionArrayOption[Long]())
    implicit val setLongOptionArray = mkArraySetOptionParameter[Long]("int8")
    implicit val setLongOptionArrayOption = mkArrayOptionSetOptionParameter[Long]("int8")
    ///
    implicit val getIntOptionArray = mkGetResult(_.nextOptionArray[Int]())
    implicit val getIntOptionArrayOption = mkGetResult(_.nextOptionArrayOption[Int]())
    implicit val setIntOptionArray = mkArraySetOptionParameter[Int]("int4")
    implicit val setIntOptionArrayOption = mkArrayOptionSetOptionParameter[Int]("int4")
    ///
    implicit val getShortOptionArray = mkGetResult(_.nextOptionArray[Short]())
    implicit val getShortOptionArrayOption = mkGetResult(_.nextOptionArrayOption[Short]())
    implicit val setShortOptionArray = mkArraySetOptionParameter[Short]("int2")
    implicit val setShortOptionArrayOption = mkArrayOptionSetOptionParameter[Short]("int2")
    ///
    implicit val getFloatOptionArray = mkGetResult(_.nextOptionArray[Float]())
    implicit val getFloatOptionArrayOption = mkGetResult(_.nextOptionArrayOption[Float]())
    implicit val setFloatOptionArray = mkArraySetOptionParameter[Float]("float4")
    implicit val setFloatOptionArrayOption = mkArrayOptionSetOptionParameter[Float]("float4")
    ///
    implicit val getDoubleOptionArray = mkGetResult(_.nextOptionArray[Double]())
    implicit val getDoubleOptionArrayOption = mkGetResult(_.nextOptionArrayOption[Double]())
    implicit val setDoubleOptionArray = mkArraySetOptionParameter[Double]("float8")
    implicit val setDoubleOptionArrayOption = mkArrayOptionSetOptionParameter[Double]("float8")
    ///
    implicit val getBoolOptionArray = mkGetResult(_.nextOptionArray[Boolean]())
    implicit val getBoolOptionArrayOption = mkGetResult(_.nextOptionArrayOption[Boolean]())
    implicit val setBoolOptionArray = mkArraySetOptionParameter[Boolean]("bool")
    implicit val setBoolOptionArrayOption = mkArrayOptionSetOptionParameter[Boolean]("bool")
    ///
    implicit val getDateOptionArray = mkGetResult(_.nextOptionArray[Date]())
    implicit val getDateOptionArrayOption = mkGetResult(_.nextOptionArrayOption[Date]())
    implicit val setDateOptionArray = mkArraySetOptionParameter[Date]("date", toStr = quotedString)
    implicit val setDateOptionArrayOption = mkArrayOptionSetOptionParameter[Date]("date", toStr = quotedString)
    ///
    implicit val getTimeOptionArray = mkGetResult(_.nextOptionArray[Time]())
    implicit val getTimeOptionArrayOption = mkGetResult(_.nextOptionArrayOption[Time]())
    implicit val setTimeOptionArray = mkArraySetOptionParameter[Time]("time", toStr = quotedString)
    implicit val setTimeOptionArrayOption = mkArrayOptionSetOptionParameter[Time]("time", toStr = quotedString)
    ///
    implicit val getTimestampOptionArray = mkGetResult(_.nextOptionArray[Timestamp]())
    implicit val getTimestampOptionArrayOption = mkGetResult(_.nextOptionArrayOption[Timestamp]())
    implicit val setTimestampOptionArray = mkArraySetOptionParameter[Timestamp]("timestamp", toStr = quotedString)
    implicit val setTimestampOptionArrayOption = mkArrayOptionSetOptionParameter[Timestamp]("timestamp", toStr = quotedString)
    ///
    implicit val getJodaDateTimeOptionArray = mkGetResult(_.nextOptionArrayMapped[Timestamp, DateTime](t => new DateTime(t.getTime, DateTimeZone.UTC)))
    implicit val getJodaDateTimeOptionArrayOption = mkGetResult(_.nextOptionArrayOptionMapped[Timestamp, DateTime](t => new DateTime(t.getTime, DateTimeZone.UTC)))
    implicit val setJodaDateTimeOptionArray = mkArraySetOptionParameter[DateTime]("timestamptz", toStr = supportNull[DateTime] {
      _.toString(jodaTzDateTimeFormatter_U)
    })
    implicit val setJodaDateTimeOptionArrayOption = mkArrayOptionSetOptionParameter[DateTime]("timestamptz", toStr = supportNull[DateTime] {
      _.toString(jodaTzDateTimeFormatter_U)
    })

    // NON OPTION, TOO
    //////////////////////////////////////////////////////////////////////////
    implicit override val getUUIDArray = mkGetResult(_.nextArray[UUID]())
    implicit override val getUUIDArrayOption = mkGetResult(_.nextArrayOption[UUID]())
    implicit override val setUUIDArray = mkArraySetParameter[UUID]("uuid", toStr = quotedString)
    implicit override val setUUIDArrayOption = mkArrayOptionSetParameter[UUID]("uuid", toStr = quotedString)
    ///
    implicit override val getStringArray = mkGetResult(_.nextArray[String]())
    implicit override val getStringArrayOption = mkGetResult(_.nextArrayOption[String]())
    implicit override val setStringArray = mkArraySetParameter[String]("text", toStr = quotedString)
    implicit override val setStringArrayOption = mkArrayOptionSetParameter[String]("text", toStr = quotedString)
    ///
    implicit override val getLongArray = mkGetResult(_.nextArray[Long]())
    implicit override val getLongArrayOption = mkGetResult(_.nextArrayOption[Long]())
    implicit override val setLongArray = mkArraySetParameter[Long]("int8")
    implicit override val setLongArrayOption = mkArrayOptionSetParameter[Long]("int8")
    ///
    implicit override val getIntArray = mkGetResult(_.nextArray[Int]())
    implicit override val getIntArrayOption = mkGetResult(_.nextArrayOption[Int]())
    implicit override val setIntArray = mkArraySetParameter[Int]("int4")
    implicit override val setIntArrayOption = mkArrayOptionSetParameter[Int]("int4")
    ///
    implicit override val getShortArray = mkGetResult(_.nextArray[Short]())
    implicit override val getShortArrayOption = mkGetResult(_.nextArrayOption[Short]())
    implicit override val setShortArray = mkArraySetParameter[Short]("int2")
    implicit override val setShortArrayOption = mkArrayOptionSetParameter[Short]("int2")
    ///
    implicit override val getFloatArray = mkGetResult(_.nextArray[Float]())
    implicit override val getFloatArrayOption = mkGetResult(_.nextArrayOption[Float]())
    implicit override val setFloatArray = mkArraySetParameter[Float]("float4")
    implicit override val setFloatArrayOption = mkArrayOptionSetParameter[Float]("float4")
    ///
    implicit override val getDoubleArray = mkGetResult(_.nextArray[Double]())
    implicit override val getDoubleArrayOption = mkGetResult(_.nextArrayOption[Double]())
    implicit override val setDoubleArray = mkArraySetParameter[Double]("float8")
    implicit override val setDoubleArrayOption = mkArrayOptionSetParameter[Double]("float8")
    ///
    implicit override val getBoolArray = mkGetResult(_.nextArray[Boolean]())
    implicit override val getBoolArrayOption = mkGetResult(_.nextArrayOption[Boolean]())
    implicit override val setBoolArray = mkArraySetParameter[Boolean]("bool")
    implicit override val setBoolArrayOption = mkArrayOptionSetParameter[Boolean]("bool")
    ///
    implicit override val getDateArray = mkGetResult(_.nextArray[Date]())
    implicit override val getDateArrayOption = mkGetResult(_.nextArrayOption[Date]())
    implicit override val setDateArray = mkArraySetParameter[Date]("date", toStr = quotedString)
    implicit override val setDateArrayOption = mkArrayOptionSetParameter[Date]("date", toStr = quotedString)
    ///
    implicit override val getTimeArray = mkGetResult(_.nextArray[Time]())
    implicit override val getTimeArrayOption = mkGetResult(_.nextArrayOption[Time]())
    implicit override val setTimeArray = mkArraySetParameter[Time]("time", toStr = quotedString)
    implicit override val setTimeArrayOption = mkArrayOptionSetParameter[Time]("time", toStr = quotedString)
    ///
    implicit override val getTimestampArray = mkGetResult(_.nextArray[Timestamp]())
    implicit override val getTimestampArrayOption = mkGetResult(_.nextArrayOption[Timestamp]())
    implicit override val setTimestampArray = mkArraySetParameter[Timestamp]("timestamp", toStr = quotedString)
    implicit override val setTimestampArrayOption = mkArrayOptionSetParameter[Timestamp]("timestamp", toStr = quotedString)
  }
}
