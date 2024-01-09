package com.mysterria.lioqu.commons.config

import com.typesafe.config.{Config, ConfigFactory}
import thesis._

import scala.collection.mutable
import scala.jdk.CollectionConverters._

object TypesafeConfigHelper {
  implicit class TypesafeConfigExtensions(underlying: Config) {
    /* ============ Common methods ============ */

    def optional[T](path: String, getter: Config => String => T): Option[T] = {
      ifDef(underlying.hasPath(path))(getter(underlying)(path))
    }

    def withDefault[T](path: String, getter: Config => String => T, default: T): T =
      optional(path, getter).getOrElse(default)

    /* ============ Boolean ============ */

    def bool(path: String, default: Boolean = false): Boolean = withDefault(path, _.getBoolean, default)

    def boolOpt(path: String): Option[Boolean] = optional(path, _.getBoolean)

    /* ============ Set ============ */

    def set[T, J](path: String, getter: Config => String => java.util.List[_ <: J], default: Set[T] = Set.empty[T])(implicit converter: J => T): Set[T] =
      setOpt(path, getter)(converter).getOrElse(default)

    def setOpt[T, J](path: String, getter: Config => String => java.util.List[_ <: J])(implicit converter: J => T): Option[Set[T]] =
      listOpt[Set[T], T, J](path, getter, _.toSet)

    def longSetOpt(path: String): Option[Set[Long]] = setOpt(path, _.getLongList)(Long2long)
    def longSet(path: String, default: Set[Long] = Set.empty): Set[Long] = longSetOpt(path).getOrElse(default)

    def intSetOpt(path: String): Option[Set[Int]] = setOpt(path, _.getIntList)(Integer2int)
    def intSet(path: String, default: Set[Int] = Set.empty): Set[Int] = intSetOpt(path).getOrElse(default)

    def doubleSetOpt(path: String): Option[Set[Double]] = setOpt(path, _.getDoubleList)(Double2double)
    def doubleSet(path: String, default: Set[Double] = Set.empty): Set[Double] = doubleSetOpt(path).getOrElse(default)

    /* ============ Sequence ============ */

    def seqOpt[T, J](path: String, getter: Config => String => java.util.List[_ <: J])(implicit converter: J => T): Option[Seq[T]] =
      listOpt[Seq[T], T, J](path, getter, _.toSeq)

    def seq[T, J](path: String, getter: Config => String => java.util.List[_ <: J], default: Seq[T] = Seq.empty[T])(implicit converter: J => T): Seq[T] =
      seqOpt(path, getter)(converter).getOrElse(default)

    def longSeqOpt(path: String): Option[Seq[Long]] = seqOpt(path, _.getLongList)(Long2long)
    def longSeq(path: String, default: Seq[Long] = Seq.empty): Seq[Long] = longSeqOpt(path).getOrElse(default)

    def intSeqOpt(path: String): Option[Seq[Int]] = seqOpt(path, _.getIntList)(Integer2int)
    def intSeq(path: String, default: Seq[Int] = Seq.empty): Seq[Int] = intSeqOpt(path).getOrElse(default)

    def doubleSeqOpt(path: String): Option[Seq[Double]] = seqOpt(path, _.getDoubleList)(Double2double)
    def doubleSeq(path: String, default: Seq[Double] = Seq.empty): Seq[Double] = doubleSeqOpt(path).getOrElse(default)

    /* ============ Auxiliary List utils ============ */

    protected def listOpt[R, SET, JET]
      (path: String, getter: Config => String => java.util.List[_ <: JET], collectionBuilder: mutable.Buffer[SET] => R)
      (implicit typeConverter: JET => SET): Option[R] = {
      optional(path, getter).map(_.asScala.map(typeConverter)).map(collectionBuilder)
    }

    /* ============ Configs ============ */

    def configOpt(path: String): Option[Config] = optional(path, _.getConfig)

    def config(path: String, default: Config = ConfigFactory.empty()): Config =
      withDefault(path, _.getConfig, default)
  }
}
