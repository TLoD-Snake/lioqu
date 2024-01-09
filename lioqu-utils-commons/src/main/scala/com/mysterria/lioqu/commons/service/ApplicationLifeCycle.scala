package com.mysterria.lioqu.commons.service

import scala.concurrent.Future

trait ApplicationLifeCycle {
  /**
    * Add a shutdown hook to be called when the application terminates.
    *
    * The stop hook should redeem the returned future when it is finished shutting down. It is acceptable to stop
    * immediately and return a successful future.
    */
  def addShutdownHook(hook: () => Future[_], name: String): Unit

  @deprecated("Use addShutdownHook instead", since = "1.6")
  def addStopHook(hook: () => Future[_], name: String): Unit = addShutdownHook(hook, name)
}
