package com.mysterria.lioqu.commons

class Default[+A](val value: A)

trait LowerPriorityImplicits {
  // Stop AnyRefs from clashing with AnyVals
  implicit def defaultNull[A <: AnyRef]:Default[A] = new Default[A](null.asInstanceOf[A])
}

object Default extends LowerPriorityImplicits {
  implicit object DefaultDouble extends Default[Double](0.0)
  implicit object DefaultFloat extends Default[Float](0.0F)
  implicit object DefaultInt extends Default[Int](0)
  implicit object DefaultLong extends Default[Long](0L)
  implicit object DefaultShort extends Default[Short](0)
  implicit object DefaultByte extends Default[Byte](0)
  implicit object DefaultChar extends Default[Char]('\u0000')
  implicit object DefaultBoolean extends Default[Boolean](false)
  implicit object DefaultUnit extends Default[Unit](())

  def value[A](implicit value: Default[A]): A = value.value
}
