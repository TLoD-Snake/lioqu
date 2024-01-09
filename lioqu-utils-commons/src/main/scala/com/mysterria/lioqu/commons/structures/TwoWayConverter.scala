package com.mysterria.lioqu.commons.structures

/**
 * Simple implementation for the two-way converter. Slow to create and isn't optimal for a big number of values.
 */
class TwoWayConverter[A, B](private val forwardConv: Map[A, B], private val backwardConv: Map[B, A]) {

  def this(fwdConv: Map[A, B]) = this(fwdConv, fwdConv.map(_.swap))

  def apply(a: A): Option[B] = fwdOpt(a)
  def unapply(b: B): Option[A] = backOpt(b)

  // function variables to declare implicit conversion
  def fwdOpt: (A => Option[B]) = forwardConv.get
  def backOpt: (B => Option[A]) = backwardConv.get

  def fwd: (A => B) = forwardConv.apply
  def back: (B => A) = backwardConv.apply
}

object TwoWayConverter {
  def apply[A, B](forwardConv: (A, B)*) = new TwoWayConverter(forwardConv.toMap)
}