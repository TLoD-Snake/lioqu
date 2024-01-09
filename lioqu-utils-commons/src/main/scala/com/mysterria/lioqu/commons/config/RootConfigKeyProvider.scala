package com.mysterria.lioqu.commons.config

trait RootConfigKeyProvider {
  val commonConfigKey: String
}

object RootConfigKeyProvider {
  def apply(_commonConfigKey: String): RootConfigKeyProvider = new RootConfigKeyProvider {
    override val commonConfigKey: String = _commonConfigKey
  }
}