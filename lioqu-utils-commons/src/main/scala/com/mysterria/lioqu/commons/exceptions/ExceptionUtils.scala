package com.mysterria.lioqu.commons.exceptions

import scala.annotation.tailrec
import scala.reflect.ClassTag

object ExceptionUtils {

  @tailrec
  def unwrapToType[T](e: Throwable)(implicit c: ClassTag[T]): Option[T] = {
    //println(s"Check ${c.runtimeClass} is assignable from ${e.getClass}")
    if (c.runtimeClass.isAssignableFrom(e.getClass)) {
      Some(e.asInstanceOf[T])
    } else if (null != e.getCause) {
      unwrapToType[T](e.getCause)
    } else {
      None
    }
  }

  implicit class ExceptionExtensions(t: Throwable) {
    def unwrapToType[T](implicit c: ClassTag[T]): Option[T] =
      ExceptionUtils.unwrapToType[T](t)
  }
}
