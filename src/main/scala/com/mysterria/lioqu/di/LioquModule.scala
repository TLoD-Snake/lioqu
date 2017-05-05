package com.mysterria.lioqu.di

import javax.inject.Singleton

import akka.actor.ActorSystem
import com.github.racc.tscg.TypesafeConfigModule
import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.ScalaModule
import com.mysterria.lioqu.config._
import com.mysterria.lioqu.db.migration.DBMigrationModule

import scala.concurrent.ExecutionContext

trait LioquModule extends AbstractModule with ScalaModule with ReadyStateBindings with LazyLogging

class LioquCoreModule extends LioquModule {

  override def configure(): Unit = {
    binder().requireExplicitBindings()
    binder().requireAtInjectOnConstructors()
    binder().requireExactBindingAnnotations()

    install(TypesafeConfigModule.fromConfigWithPackage(config, LioquCoreModule.LioquPackage))
    install(TypesafeConfigModule.fromConfigWithPackage(config, config.getString(AppPackage)))

    install(new RepositoryModule)
    install(new HttpModule)
    install(new ServiceModule)
    install(new DBMigrationModule(config))
    install(new AppConnectorModule(config))
  }

  @Provides
  @Singleton
  def configProvider(): Config = config

  @Provides
  @Singleton
  def asProvider(): ActorSystem = {
    ActorSystem("MainActorSystem")
  }

  @Provides
  def ecProvider(as: ActorSystem): ExecutionContext = {
    as.dispatcher
  }

  protected def config: Config = LioquCoreModule.config
}

object LioquCoreModule extends LazyLogging {
  private val config = ConfigFactory.load()
  logger.info(s"App config:\n - origin ${config.origin()}\n - content: $config")

  final val LioquPackage = "com.mysterria.lioqu"
}
