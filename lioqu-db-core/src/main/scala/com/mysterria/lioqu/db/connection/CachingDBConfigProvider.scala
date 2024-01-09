package com.mysterria.lioqu.db.connection
import java.util.concurrent.ConcurrentHashMap

import slick.basic.DatabaseConfig
import scala.jdk.CollectionConverters._
import thesis._
import scala.reflect.ClassTag

class CachingDBConfigProvider[T <: slick.basic.BasicProfile : ClassTag] extends DBConfigProvider[T] {
  private val cache = new ConcurrentHashMap[String, DatabaseConfig[T]](4).asScala

  override def conf(s: String): DatabaseConfig[T] = cache.computeIfAbsent(s, GenericDBConfigProvider.conf)

  override def schema(configPath: String): Option[String] = GenericDBConfigProvider.schema(conf(configPath))
}
