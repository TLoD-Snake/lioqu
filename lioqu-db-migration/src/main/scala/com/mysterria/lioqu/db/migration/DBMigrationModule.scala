package com.mysterria.lioqu.db.migration

import javax.inject.Singleton
import com.google.inject.TypeLiteral
import com.google.inject.multibindings.MapBinder
import com.mysterria.lioqu.config.Lioqu
import com.mysterria.lioqu.di.{AppModule, LioquModule}
import com.typesafe.config.Config
import org.reflections.Reflections

import scala.collection.JavaConverters._
import scala.language.postfixOps

@AppModule
class DBMigrationModule(globalConfig: Config) extends LioquModule {
  import DBMigrationModule._

  override def configure(): Unit = {
    bindInjectedMigrationsMap()
    bind[DbMigrationService].asEagerSingleton()
    readyStateBinder(binder).addBinding.to[DbMigrationService]
  }

  private def bindInjectedMigrationsMap() = {
    val injectedMigrationsBindings = MapBinder.newMapBinder(binder,
      new TypeLiteral[MigrationDescriptor]{},
      new TypeLiteral[Seq[Class[_ <: LioquInjectedMigration]]]{}
    )
    scanInjectedMigrations foreach { case(descriptor, ims) =>
      injectedMigrationsBindings.addBinding(descriptor).toInstance(ims)
    }
  }

  private def scanInjectedMigrations: Map[MigrationDescriptor, Seq[Class[_ <: LioquInjectedMigration]]] = {
    descriptors map { descriptor =>
      val reflections = new Reflections(descriptor.location)
      val classes = reflections.getSubTypesOf(classOf[LioquInjectedMigration]).asScala
      classes foreach { t =>
        bind(t).in(classOf[Singleton])
      }
      descriptor -> classes.toSeq
    } toMap
  }

  private def descriptors: Seq[MigrationDescriptor] = {
    val migDescriptorsConfig = globalConfig.getConfig(MigrationDescriptorsConfigPath)
    val descriptorKeys = migDescriptorsConfig.root().entrySet().asScala.map{_.getKey}.toSeq
    descriptorKeys map {name =>
      MigrationDescriptor.fromConfig(name, migDescriptorsConfig.getConfig(name))
    }
  }
}

object DBMigrationModule {
  final val MigrationConfigPath = s"$Lioqu.migration"
  final val MigrationDescriptorsConfigPath = s"$MigrationConfigPath.descriptors"
}
