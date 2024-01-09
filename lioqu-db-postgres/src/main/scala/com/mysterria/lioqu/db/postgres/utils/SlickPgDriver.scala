package com.mysterria.lioqu.db.postgres.utils

import com.github.tminglei.slickpg._
import play.api.libs.json.{JsValue, Json}
import slick.jdbc.{GetResult, PositionedResult, SetParameter}

trait SlickPgDriver extends ExPostgresProfile
                       with PgJavaBoxArraySupport
                       with FixedPgDateSupportJoda
                       with PgRangeSupport
                       with PgPlayJsonSupport
{
  override def pgjson = "json"

  override val api = PgSlickAPI

  object PgSlickAPI extends API with OptionArrayPlainImplicits
                                with FixedJodaDateTimeImplicits
                                with FixedJodaDateTimePlainImplicits
                                with JsonImplicits
                                with PlayJsonPlainImplicits {
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val playJsonArrayTypeMapper =
      new AdvancedArrayJdbcType[JsValue](pgjson,
        (s) => utils.SimpleArrayUtils.fromString[JsValue](Json.parse)(s).orNull,
        (v) => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)
      ).to(_.toList)

    implicit val jsonArraySetOptionParameter: SetParameter[Seq[Option[JsValue]]] = mkArraySetOptionParameter[JsValue](
      pgjson,
      (v: JsValue) => Json.stringify(v)
    )

    class GetTupleResult2[+T <: Product](val children: GetResult[_]*) extends GetResult[T] {
      def apply(rs: PositionedResult) = {
        val s = children.iterator.map(_.apply(rs)).toIndexedSeq

        val r = s.length match {
          case 24 => new Tuple24(s(0), s(1), s(2), s(3), s(4), s(5), s(6), s(7), s(8), s(9), s(10), s(11), s(12), s(13), s(14), s(15), s(16), s(17), s(18), s(19), s(20), s(21), s(22), s(23))
          case _ => throw new RuntimeException(s"$this requires exactly 24 children")
        }

        r.asInstanceOf[T]
      }
      override def toString() = "GetTupleResult2<"+children.length+">"
    }

    @inline implicit def createGetTuple24[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, T23, T24](implicit c1: GetResult[T1], c2: GetResult[T2], c3: GetResult[T3], c4: GetResult[T4], c5: GetResult[T5], c6: GetResult[T6], c7: GetResult[T7], c8: GetResult[T8], c9: GetResult[T9], c10: GetResult[T10], c11: GetResult[T11], c12: GetResult[T12], c13: GetResult[T13], c14: GetResult[T14], c15: GetResult[T15], c16: GetResult[T16], c17: GetResult[T17], c18: GetResult[T18], c19: GetResult[T19], c20: GetResult[T20], c21: GetResult[T21], c22: GetResult[T22], c23: GetResult[T23], c24: GetResult[T24]): GetTupleResult2[Tuple24[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, T23, T24]] = new GetTupleResult2[Tuple24[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, T23, T24]](c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18, c19, c20, c21, c22, c23, c24)
  }
}

object SlickPgDriver extends SlickPgDriver