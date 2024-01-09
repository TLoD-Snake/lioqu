package com.mysterria.lioqu

package object config {
  final val Lioqu = "lioqu"
  final val Prefix = s"$Lioqu."

  final val LifeCycle = Prefix + "life-cycle."
  /**
    * Determines for how long lioqu will wait for each service to terminate
    */
  final val LifeCycleServiceTerminationTimeout = LifeCycle + "service-termination-timeout"

  final val PidPath = Prefix + "pid-path"
}
