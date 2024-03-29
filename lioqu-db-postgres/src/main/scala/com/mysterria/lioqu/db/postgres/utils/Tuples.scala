package com.mysterria.lioqu.db.postgres.utils

import scala.runtime.ScalaRunTime

// taken from https://github.com/fwbrasil/activate/tree/master/activate-core/src/main/scala/net/fwbrasil/scala

trait CustomTuple {
  this: Product =>

  override def toString() = "(" + this.productIterator.mkString(", ") + ")"

  override def hashCode(): Int = ScalaRunTime._hashCode(this)

  override def equals(other: Any): Boolean =
    if (canEqual(other)) {
      val otherTuple = other.asInstanceOf[Tuple24[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]]
      (0 until this.productArity).forall(i => this.productElement(i) == otherTuple.productElement(i))
    } else false

}

object Product24 {
  def unapply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, T23, T24](x: Product24[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, T23, T24]): Option[Product24[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, T23, T24]] =
    Some(x)
}

trait Product24[+T1, +T2, +T3, +T4, +T5, +T6, +T7, +T8, +T9, +T10, +T11, +T12, +T13, +T14, +T15, +T16, +T17, +T18, +T19, +T20, +T21, +T22, +T23, +T24] extends Product {

  override def productArity = 24

  @throws(classOf[IndexOutOfBoundsException])
  override def productElement(n: Int) = n match {
    case 0  => _1
    case 1  => _2
    case 2  => _3
    case 3  => _4
    case 4  => _5
    case 5  => _6
    case 6  => _7
    case 7  => _8
    case 8  => _9
    case 9  => _10
    case 10 => _11
    case 11 => _12
    case 12 => _13
    case 13 => _14
    case 14 => _15
    case 15 => _16
    case 16 => _17
    case 17 => _18
    case 18 => _19
    case 19 => _20
    case 20 => _21
    case 21 => _22
    case 22 => _23
    case 23 => _24
    case _  => throw new IndexOutOfBoundsException(n.toString())
  }

  def _1: T1
  def _2: T2
  def _3: T3
  def _4: T4
  def _5: T5
  def _6: T6
  def _7: T7
  def _8: T8
  def _9: T9
  def _10: T10
  def _11: T11
  def _12: T12
  def _13: T13
  def _14: T14
  def _15: T15
  def _16: T16
  def _17: T17
  def _18: T18
  def _19: T19
  def _20: T20
  def _21: T21
  def _22: T22
  def _23: T23
  def _24: T24

}

class Tuple24[+T1, +T2, +T3, +T4, +T5, +T6, +T7, +T8, +T9, +T10, +T11, +T12, +T13, +T14, +T15, +T16, +T17, +T18, +T19, +T20, +T21, +T22, +T23, +T24](val _1: T1, val _2: T2, val _3: T3, val _4: T4, val _5: T5, val _6: T6, val _7: T7, val _8: T8, val _9: T9, val _10: T10, val _11: T11, val _12: T12, val _13: T13, val _14: T14, val _15: T15, val _16: T16, val _17: T17, val _18: T18, val _19: T19, val _20: T20, val _21: T21, val _22: T22, val _23: T23, val _24: T24)
  extends Product24[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, T23, T24]
  with CustomTuple {

  override def canEqual(other: Any) =
    other.isInstanceOf[Tuple24[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]]

}