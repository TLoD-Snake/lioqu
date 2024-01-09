package com.mysterria.lioqu.di

import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Provides}
import com.mysterria.lioqu.config.LioquCoreConfig
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Singleton
import net.codingwell.scalaguice.ScalaModule
import thesis._

import scala.concurrent.ExecutionContext

trait LioquModule extends AbstractModule
  with ScalaModule
  with ReadyStateBindings
  with ActorFactoryBindingSupport
  with LazyLogging

class LioquCoreModule extends LioquModule {

  override def configure(): Unit = {
    binder().requireExplicitBindings()
    binder().requireAtInjectOnConstructors()
    binder().requireExactBindingAnnotations()
    readyStateBinder(binder) // Init

    install(new ServiceModule)
    install(new AppConnectorModule(coreConfig, config))
  }

  @Provides
  @Singleton
  def coreConfigProvider(): LioquCoreConfig = coreConfig

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

  protected val config: Config = LioquCoreModule.loadConfig()
  protected val coreConfig: LioquCoreConfig = LioquCoreConfig.parseFromGlobalConfig(config)
}

object LioquCoreModule extends LazyLogging {
  private def loadConfig(): Config = con(ConfigFactory.load()) { config =>
    logger.info(s"App config:\n - origin ${config.origin()}\n - content: $config")
  }
}
