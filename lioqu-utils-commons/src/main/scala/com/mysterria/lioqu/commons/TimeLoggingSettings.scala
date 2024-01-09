package com.mysterria.lioqu.commons

import scala.annotation.implicitNotFound

@implicitNotFound("""Cannot find an implicit TimeLoggingSettings. You might pass
an (implicit settings: TimeLoggingSettings) parameter to your method
or import thesis.Implicits.defaultTimeLoggingSettings""")
case class TimeLoggingSettings(traceTreshold: Int = 50, debugThreshold: Int = 300,
                               infoThreshold: Int = 800, warnThreshold: Int = 2500)
