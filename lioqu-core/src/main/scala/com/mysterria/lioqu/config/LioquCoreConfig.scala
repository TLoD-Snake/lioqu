package com.mysterria.lioqu.config

import com.mysterria.lioqu.tools.LioquConfigHelper
import com.typesafe.config.Config

/**
  *
  * @param suppressNoAppPackageWarning You can suppress Lioqu warning about no AppPackages were found by Reflections
  * @param appPackage Reflections will use this value to scan for annotations
  *                   Should be properly set up during project initialization via 'sbt new' command
  *                   if tlod-snake/lioqu-template.g8 template used
  * @param appModules Set of fully qualified class names of modules to be installed by Lioqu
  * @param scanForModules If enabled Lioqu will scan appPackage package for modules annotated with @AppModule
  */
case class LioquCoreConfig(
  suppressNoAppPackageWarning: Boolean,
  appPackage: Option[String],
  scanForModules: Boolean,
  appModules: Set[String]
)


object LioquCoreConfig {
  case object ConfigKeys {
    val SuppressNoAppPackageWarning = "suppress-no-app-package-warning"
    val AppPackage = "app-package"
    val AppModules = "app-modules"
    val ScanForModules = "scan-for-modules"
  }

  /**
    * Parses Lioqu Core configuration from the global application config. You don't need to extract lioqu node for this method to work
    * @param config global app config
    */
  def parseFromGlobalConfig(config: Config): LioquCoreConfig = LioquCoreConfig(config.getConfig(Lioqu))

  /**
    * Parses lioqu config node into LioquCoreConfig object
    */
  def apply(config: Config): LioquCoreConfig = {
    import ConfigKeys._
    val helper = LioquConfigHelper(config)
    new LioquCoreConfig(
      suppressNoAppPackageWarning = helper.withDefault(SuppressNoAppPackageWarning, _.getBoolean, false),
      appPackage = helper.optional(AppPackage, _.getString),
      appModules = helper.setOrDefault(AppModules, _.getStringList),
      scanForModules = helper.withDefault(ScanForModules, _.getBoolean, false)
    )
  }
}