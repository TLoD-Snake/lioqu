package com.mysterria.lioqu.commons

import play.api.libs.json._
import thesis._

object EnumUtils {
  def enumReads[E <: Enumeration](enum: E, ignoreCase: Boolean = false): Reads[E#Value] = new Reads[E#Value] {
    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsString(s) =>
        try {
          JsSuccess(if(ignoreCase) enum.withNameIgnoreCase(s) else enum.withName(s))
        } catch {
          case _: NoSuchElementException => JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
        }
      case _ => JsError("String value expected")
    }
  }

  def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsString(v.toString)
  }

  def enumFormat[E <: Enumeration](enum: E, caseInsensitiveReads: Boolean = false): Format[E#Value] = {
    Format(EnumUtils.enumReads(enum, caseInsensitiveReads), EnumUtils.enumWrites)
  }
}