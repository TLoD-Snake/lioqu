package com.mysterria.lioqu.tools

import com.typesafe.config.Config
import scala.jdk.CollectionConverters._

import thesis._

case class LioquConfigHelper(config: Config) {

  def optional[T](path: String, getter: Config => String => T): Option[T] = {
    ifDef(config.hasPath(path))(getter(config)(path))
  }

  def withDefault[T](path: String, getter: Config => String => T, default: T): T =
    optional(path, getter).getOrElse(default)

  def setOrDefault[T](path: String, getter: Config => String => java.util.List[T], default: Set[T] = Set.empty[T]): Set[T] =
    optional(path, getter).map(_.asScala.toSet).getOrElse(default)

}
