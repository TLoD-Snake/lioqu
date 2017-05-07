package com.mysterria.lioqu.db.migration

import org.flywaydb.core.api.{FlywayException, MigrationVersion}
import org.flywaydb.core.api.migration.MigrationInfoProvider
import org.flywaydb.core.internal.resolver.MigrationInfoHelper
import org.flywaydb.core.internal.util.{ClassUtils, Pair}

import scala.concurrent.Future

trait LioquInjectedMigration extends MigrationInfoProvider {

  def migrate(): Future[Unit]

  val info: (MigrationVersion, String) = parseName

  override def getVersion: MigrationVersion = info._1
  override def getDescription: String = info._2

  protected def parseName: (MigrationVersion, String) = {
    val shortName = ClassUtils.getShortName(getClass)
    val repeatable = shortName.startsWith("R")

    var prefix: String = null
    if (shortName.startsWith("V") || repeatable) {
      prefix = shortName.substring(0, 1)
    } else throw new FlywayException(s"Invalid migration class name: ${getClass.getName} ensure it starts with V or R, or implement org.flywaydb.core.api.migration.MigrationInfoProvider for non-default naming")

    val info = MigrationInfoHelper.extractVersionAndDescription(shortName, prefix, "__", "", repeatable)
    (info.getLeft, info.getRight)
  }
}
