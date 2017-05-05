package com.mysterria.lioqu.db.migration

import java.io.PrintWriter
import java.sql.Connection
import java.util.logging.Logger
import javax.inject._
import javax.sql.DataSource

import com.google.inject.Injector
import com.mysterria.lioqu.db.connection.GenericDBConfigProvider
import com.mysterria.lioqu.db.migration.migrations.LioquMigrationResolver
import com.mysterria.lioqu.di.ReadyStateService
import com.typesafe.scalalogging.LazyLogging
import org.flywaydb.core.Flyway
import slick.jdbc.{JdbcDataSource, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.collection.JavaConverters._

class DbMigrationService @Inject()(
  descriptors: java.util.Map[MigrationDescriptor, Seq[Class[_ <: LioquInjectedMigration]]],
  injector: Injector
)(implicit ec: ExecutionContext) extends ReadyStateService with LazyLogging {

  private val readyPromise = Promise[Unit]
  override def ready: Future[Unit] = readyPromise.future

  try {
    migrate()
    readyPromise.trySuccess(())
  } catch { case t: Throwable =>
    logger.error(s"DB migration failed: ${t.getMessage}", t)
    readyPromise.tryFailure(t)
  }

  private def migrate(): Unit = {
    logger.info("DB Migration Service started")
    descriptors.asScala foreach { case(descriptor, injectedMigrationClasses) =>
      logger.info(s"Got migration descriptor: $descriptor")

      val location = s"classpath:${descriptor.location}"
      val ds = GenericDBConfigProvider.basicConf(descriptor.dbConfigPath).db.asInstanceOf[JdbcProfile#Backend#Database].source
      val schema = GenericDBConfigProvider.schema(descriptor.dbConfigPath)
      val resolver = new LioquMigrationResolver(injectedMigrationClasses, injector)

      val flyway: Flyway = new Flyway()
      flyway.setDataSource(slickDs2jDs(ds))
      flyway.setResolvers(resolver)
      schema.foreach { s =>
        logger.info(s"Setting schema to $s")
        flyway.setSchemas(s)
      }
      logger.info(s"Setting location to '$location'")
      flyway.setLocations(location)
      flyway.setBaselineOnMigrate(descriptor.baselineOnMigrate)
      flyway.migrate()
    }
  }

  private def slickDs2jDs(ds: JdbcDataSource): javax.sql.DataSource = {
    def unsupported =
      throw new UnsupportedOperationException("Unsupported in fake Datasource. This class is only capable of returning connection.")

    new DataSource {
      override def getConnection: Connection = ds.createConnection()
      override def getConnection(username: String, password: String): Connection = ds.createConnection()
      override def unwrap[T](iface: Class[T]): T = unsupported
      override def isWrapperFor(iface: Class[_]): Boolean = unsupported
      override def setLoginTimeout(seconds: Int): Unit = unsupported
      override def setLogWriter(out: PrintWriter): Unit = unsupported
      override def getParentLogger: Logger = unsupported
      override def getLoginTimeout: Int = unsupported
      override def getLogWriter: PrintWriter = unsupported
    }
  }
}
