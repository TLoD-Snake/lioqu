package com.mysterria.lioqu.di

import com.google.inject.AbstractModule
import com.typesafe.config.Config
import org.reflections.Reflections
import com.mysterria.lioqu.config._

import scala.collection.JavaConverters._

class AppConnectorModule(config: Config) extends LioquModule {
  import AppConnectorModule._

  override def configure(): Unit = {
    val pack = config.getString(AppPackage)
    val reflections = new Reflections(pack)
    val moduleTypes = reflections.getTypesAnnotatedWith(AppConnectorModule.appAnnotation).asScala

    val modules = moduleTypes.flatMap { annotatedClass =>
      if (!(moduleInterface isAssignableFrom annotatedClass)) {
        val message = s"Class ${annotatedClass.getName} is annotated with ${appAnnotation.getName}, but does not implement ${moduleInterface.getName}"
        logger.error(message)
        throw new RuntimeException(message)
      }
      try {
        Some(moduleInterface.cast(annotatedClass.getConstructor().newInstance()))
      } catch {
        case err: Throwable =>
          logger.error(s"Error creating Guice Module from ${annotatedClass.getName}: ${err.getMessage}", err)
          None
      }
    }

    if (modules.nonEmpty) {
      modules.foreach { module =>
        logger.info(s"Installing $module App Module")
        install(module)
      }
    } else {
      logger.error(s"No AppModules detected for package $pack and its descendants. Your app most probably didn't start.")
    }
  }
}

object AppConnectorModule {
  final val appAnnotation: Class[AppModule] = classOf[AppModule]
  final val moduleInterface: Class[AbstractModule] = classOf[AbstractModule]
}
