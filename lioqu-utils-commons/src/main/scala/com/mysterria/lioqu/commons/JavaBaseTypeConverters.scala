package com.mysterria.lioqu.commons

object JavaBaseTypeConverters {
  implicit class byteBoxer(x: Byte) { def toJavaBox = byte2Byte(x) }
  implicit class byteOptionBoxer(x: Option[Byte]) { def toJavaBox = x.map(byte2Byte).orNull }

  implicit class shortBoxer(x: Short) { def toJavaBox = short2Short(x) }
  implicit class shortOptionBoxer(x: Option[Short]) { def toJavaBox = x.map(short2Short).orNull }

  implicit class charBoxer(x: Char) { def toJavaBox = char2Character(x) }
  implicit class charOptionBoxer(x: Option[Char]) { def toJavaBox = x.map(char2Character).orNull }

  implicit class intBoxer(x: Int) { def toJavaBox = int2Integer(x) }
  implicit class intOptionBoxer(x: Option[Int]) { def toJavaBox = x.map(int2Integer).orNull }

  implicit class longBoxer(x: Long) { def toJavaBox = long2Long(x) }
  implicit class longOptionBoxer(x: Option[Long]) { def toJavaBox = x.map(long2Long).orNull }

  implicit class floatBoxer(x: Float) { def toJavaBox = float2Float(x) }
  implicit class floatOptionBoxer(x: Option[Float]) { def toJavaBox = x.map(float2Float).orNull }

  implicit class doubleBoxer(x: Double) { def toJavaBox = double2Double(x) }
  implicit class doubleOptionBoxer(x: Option[Double]) { def toJavaBox = x.map(double2Double).orNull }

  implicit class booleanBoxer(x: Boolean) { def toJavaBox = boolean2Boolean(x) }
  implicit class booleanOptionBoxer(x: Option[Boolean]) { def toJavaBox = x.map(boolean2Boolean).orNull }
}
