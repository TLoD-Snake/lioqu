package com.mysterria.lioqu.db.postgres.utils

import com.github.tminglei.slickpg.PgDateSupportJoda
import com.github.tminglei.slickpg.utils.PlainSQLUtils._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatterBuilder
import slick.jdbc.{GetResult, PositionedResult, PostgresProfile}

import java.sql.Types

trait FixedPgDateSupportJoda extends PgDateSupportJoda {
  driver: PostgresProfile =>

  trait FixedJodaDateTimeFormatters extends JodaDateTimeFormatters {
    val jodaTzTimeFormatter_U =
      new DateTimeFormatterBuilder().append(
        jodaTzTimeFormatter.getPrinter,
        Array(jodaTimeFormatter.getParser, jodaTzTimeFormatter.getParser)
      ).toFormatter
    val jodaTzTimeFormatter_NoFraction_U =
      new DateTimeFormatterBuilder().append(
        jodaTzTimeFormatter_NoFraction.getPrinter,
        Array(jodaTimeFormatter_NoFraction.getParser, jodaTzTimeFormatter_NoFraction.getParser)
      ).toFormatter
    val jodaTzDateTimeFormatter_U =
      new DateTimeFormatterBuilder().append(
        jodaTzDateTimeFormatter.getPrinter,
        Array(jodaDateTimeFormatter.getParser, jodaTzDateTimeFormatter.getParser)
      ).toFormatter
    val jodaTzDateTimeFormatter_NoFraction_U =
      new DateTimeFormatterBuilder().append(
        jodaTzDateTimeFormatter_NoFraction.getPrinter,
        Array(jodaDateTimeFormatter_NoFraction.getParser, jodaTzDateTimeFormatter_NoFraction.getParser)
      ).toFormatter
  }

  trait FixedJodaDateTimeImplicits extends JodaDateTimeImplicits with FixedJodaDateTimeFormatters {
    override implicit val jodaTimestampTZTypeMapper: GenericJdbcType[DateTime] = new GenericJdbcType[DateTime]("timestamptz",
      fnFromString = (s) => DateTime.parse(s,
        if (s.indexOf(":") > 2) { if (s.indexOf(".") > 0) jodaTzDateTimeFormatter_U else jodaTzDateTimeFormatter_NoFraction_U }
        else { if (s.indexOf(".") > 0) jodaTzTimeFormatter_U else jodaTzTimeFormatter_NoFraction_U }),
      fnToString = (v) => v.toString(jodaTzDateTimeFormatter),
      hasLiteralForm = false)
  }

  trait FixedJodaDateTimePlainImplicits extends JodaDateTimePlainImplicits with FixedJodaDateTimeFormatters {
    implicit class PgDate2TimePositionedResult_U(r: PositionedResult) {
      def nextZonedDateTime_U() = nextZonedDateTimeOption_U().orNull
      def nextZonedDateTimeOption_U() = r.nextStringOption().map(s => DateTime.parse(s,
        if(s.indexOf(".") > 0 ) jodaTzDateTimeFormatter_U else jodaTzDateTimeFormatter_NoFraction_U))
    }

    override implicit val getZonedDateTime: GetResult[DateTime] = mkGetResult(_.nextZonedDateTime_U())
    override implicit val getZonedDateTimeOption: GetResult[Option[DateTime]] = mkGetResult(_.nextZonedDateTimeOption_U())

    override implicit val setZonedDateTime = mkSetParameter[DateTime]("timestamptz", _.toString(jodaTzDateTimeFormatter), sqlType = Types.TIMESTAMP)
    override implicit val setZonedDateTimeOption = mkOptionSetParameter[DateTime]("timestamptz", _.toString(jodaTzDateTimeFormatter), sqlType = Types.TIMESTAMP)
  }
}
