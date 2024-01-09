package com.mysterria.lioqu.di

import com.google.inject.Module
import com.mysterria.lioqu.config._
import com.typesafe.config.Config
import org.reflections.Reflections

import scala.jdk.CollectionConverters._

class AppConnectorModule(coreConfig: LioquCoreConfig, config: Config) extends LioquModule {
  import AppConnectorModule._

  override def configure(): Unit = {
    val packages: Set[String] = coreConfig.appPackage.toSet
    logger.info(s"Searching application modules in ${packages.mkString(", ")}")

    val moduleClasses = packages.flatMap(findModuleClasses) ++ parseConfigAppModules
    logger.info(s"Found module classes: ${moduleClasses.map(_.getSimpleName).mkString(", ")}")

    val modules = moduleClasses map instance

    if (modules.nonEmpty) {
      modules foreach installModule
    } else if(!coreConfig.suppressNoAppPackageWarning) {
      logger.warn(s"No AppModules detected for packages ${packages.mkString(", ")} and its descendants. Your app most probably didn't start.")
    }
  }

  private def installModule(module: Module): Unit = {
    logger.info(s"Installing $module App Module")
    install(module)
  }

  private def findModuleClasses(packageName: String): Set[Class[_]] = {
    val reflections = new Reflections(packageName)
    reflections.getTypesAnnotatedWith(AppConnectorModule.AppAnnotation).asScala.toSet
  }

  private def instance(clazz: Class[_]): Module = {
    if (!(ModuleInterface isAssignableFrom clazz))
      throw new ClassCastException(s"Class ${clazz.getName} is annotated with ${AppAnnotation.getName}, but does not implement ${ModuleInterface.getName}")

    clazz.getConstructors
      .find(_.getParameterTypes.sameElements(Seq(classOf[Config])))
      .map { c => c.newInstance(config) }
      .getOrElse(clazz.getDeclaredConstructor().newInstance())
      .asInstanceOf[Module]
  }

  private def parseConfigAppModules: Set[Class[_]] = coreConfig.appModules map Class.forName
}

object AppConnectorModule {
  final val AppAnnotation: Class[AppModule] = classOf[AppModule]
  final val ModuleInterface: Class[Module] = classOf[Module]
}
