package com.mysterria.lioqu.commons

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json.{JsError, JsSuccess, Json}


class EnumUtilsTest extends AnyFlatSpec {
  import EnumUtilsTest._

  "Generated Reads" should "process case sensitive enums" in {
    implicit val enumReads = EnumUtils.enumReads(Nums)
    implicit val modelReads = Json.reads[Model]
    val okRes = Json.parse(caseOKString).validate[Model]
    okRes should matchPattern { case JsSuccess(Model(Nums.One), _) => }

    val failRes = Json.parse(caseFailedString).validate[Model]
    failRes should matchPattern { case JsError(_) => }
  }

  "Generated Reads" should "process case insensitive enums" in {
    implicit val enumReads = EnumUtils.enumReads(Nums, ignoreCase = true)
    implicit val modelReads = Json.reads[Model]
    Seq(caseOKString, caseFailedString) foreach { str =>
      val res = Json.parse(str).validate[Model]
      res should matchPattern { case JsSuccess(Model(Nums.One), _) => }
    }
  }

  "Generated Format" should "process case sensitive enums" in {
    implicit val enumFormat = EnumUtils.enumFormat(Nums)
    implicit val modelFormat = Json.format[Model]
    val okRes = Json.parse(caseOKString).validate[Model]
    okRes should matchPattern { case JsSuccess(Model(Nums.One), _) => }

    val failRes = Json.parse(caseFailedString).validate[Model]
    failRes should matchPattern { case JsError(_) => }
  }

  "Generated Format" should "process case insensitive enums" in {
    implicit val enumFormat = EnumUtils.enumFormat(Nums, caseInsensitiveReads = true)
    implicit val modelFormat = Json.format[Model]
    Seq(caseOKString, caseFailedString) foreach { str =>
      val res = Json.parse(str).validate[Model]
      res should matchPattern { case JsSuccess(Model(Nums.One), _) => }
    }
  }
}

object EnumUtilsTest {
  object Nums extends Enumeration {
    val One = Value(1)
  }

  case class Model(number: Nums.Value)

  val caseOKString = "{\"number\": \"One\"}"
  val caseFailedString = "{\"number\": \"onE\"}"
}
