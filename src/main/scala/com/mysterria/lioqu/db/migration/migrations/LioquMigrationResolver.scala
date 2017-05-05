package com.mysterria.lioqu.db.migration.migrations

import java.util
import com.google.inject.Injector
import com.mysterria.lioqu.db.migration.LioquInjectedMigration
import com.typesafe.scalalogging.LazyLogging
import org.flywaydb.core.api.MigrationType
import org.flywaydb.core.api.migration.MigrationChecksumProvider
import org.flywaydb.core.api.resolver.{BaseMigrationResolver, ResolvedMigration}
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl
import scala.collection.JavaConverters._

class LioquMigrationResolver(classes: Seq[Class[_ <: LioquInjectedMigration]], injector: Injector) extends BaseMigrationResolver with LazyLogging {

  override def resolveMigrations(): util.Collection[ResolvedMigration] = {
    logger.debug(s"Got classes: $classes")
    val migrations = classes map {t =>
      injector.getInstance(t)
    }
    logger.debug(s"Got migrations: $migrations")

    val resolvedMigrations = migrations map { m =>
      val checksum: Integer = m match {
        case provider: MigrationChecksumProvider => provider.getChecksum
        case _ => null
      }

      val resolvedMigration = new ResolvedMigrationImpl
      resolvedMigration.setVersion(m.getVersion)
      resolvedMigration.setDescription(m.getDescription)
      resolvedMigration.setScript(m.getClass.getName)
      resolvedMigration.setChecksum(checksum)
      resolvedMigration.setType(MigrationType.CUSTOM)
      resolvedMigration.asInstanceOf[ResolvedMigration]
    }
    resolvedMigrations.asJava
  }
}
