package com.mysterria.lioqu.http.di

import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Binder}
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}

trait RouteBindings extends AbstractModule with ScalaModule {

  def routeBinder(binder: Binder) = ScalaMultibinder.newSetBinder[RouteProvider](binder, Names.named("routes"))

}
