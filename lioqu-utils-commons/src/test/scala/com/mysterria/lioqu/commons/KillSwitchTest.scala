package com.mysterria.lioqu.commons

import org.scalatest.Succeeded
import org.scalatest.flatspec.AsyncFlatSpec
import thesis._

import scala.concurrent.{Future, Promise}

class KillSwitchTest extends AsyncFlatSpec {

  classOf[KillSwitch].getSimpleName should "work" in {
    val restarted = Promise[(Option[String], Option[Throwable])]()
    val stopped = Promise[(Option[String], Option[Throwable])]()

    def restart(m: Option[String], c: Option[Throwable]) =
      con(restarted)(_.success((m, c))).future
    def stop(m: Option[String], c: Option[Throwable]) =
      con(stopped)(_.success((m, c))).future

    val killSwitch = KillSwitch(restartOp = restart, stopOp = stop)

    assert(!restarted.isCompleted)
    assert(!stopped.isCompleted)

    val restartMessage = "restarting it"
    val restartCause = new Exception(restartMessage)
    killSwitch.restart(restartMessage, restartCause)

    assert(restarted.isCompleted)
    assert(!stopped.isCompleted)

    val stopMessage = "restarting it"
    val stopCause = new Exception(restartMessage)
    killSwitch.stop(stopMessage, stopCause)

    assert(restarted.isCompleted)
    assert(stopped.isCompleted)

    val results = Seq(
      restarted.future.map { case (m, c) =>
        assert(m.contains(restartMessage) && c.contains(restartCause))
      },
      stopped.future.map { case (m, c) =>
        assert(m.contains(stopMessage) && c.contains(stopCause))
      }
    )

    Future.sequence(results).map(r => assert(r.forall(_ == Succeeded)))
  }

}
