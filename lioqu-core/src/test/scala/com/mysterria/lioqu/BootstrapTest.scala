package com.mysterria.lioqu

import com.google.inject.Guice
import com.mysterria.lioqu.di.LioquCoreModule
import org.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec

class BootstrapTest extends AnyFlatSpec with MockitoSugar {
  "Lioqu" should "bootstrap without exceptions" in {
    Guice.createInjector(new LioquCoreModule)
  }
}
