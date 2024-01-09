package com.mysterria.lioqu.service

import java.util.concurrent.atomic.AtomicInteger

import javax.inject._
import akka.actor.ActorSystem
import com.mysterria.lioqu.commons.service.{ApplicationLifeCycle, ReadyStateService}
import com.mysterria.lioqu.config._
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scala.collection.immutable
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._

import thesis._

class LifeCycleService @Inject()(
  as: ActorSystem,
  waitableServices: immutable.Set[ReadyStateService],
  config: Config
)(implicit ec: ExecutionContext) extends ApplicationLifeCycle with LazyLogging {
  type Waitable = Class[_ <: ReadyStateService]

  private val terminatables = new AtomicInteger(0)
  private val terminationTimeout = config.getDuration(LifeCycleServiceTerminationTimeout)

  awaitAllStates foreach { states =>
    val statesStr = states.map(st => s"\t${st._1} => ${st._2.map{_=>"Success"}.getOrElse("Failure")}").mkString("\n")
    logger.info(s"All services done initilization:\n$statesStr")
  }

  waitableServices.toSeq foreach { s =>
    registerOnTermination(() => s.terminate(), s.toString)
  }

  // On SIGTERM we shut down Actor System
  sys.addShutdownHook{
    logger.info("Lioqu terminates. Bye. =^_^=")
    as.terminate()
    Await.ready(as.whenTerminated, 10 second)
  }

  private def registerOnTermination(hook: () => Future[_], name: String): Unit = {
    terminatables.incrementAndGet()
    as.registerOnTermination {
      val i = terminatables.getAndDecrement()
      logger.info(s"Service '$name' termination started (#$i)")
      Try(Await.ready(hook(), terminationTimeout)) match {
        case Success(_) => logger.info(s"Service '$name' terminated (#$i)")
        case Failure(t) => logger.error(s"Service '$name' unable to terminate for $terminationTimeout (#$i)", t)
      }
    }
  }

  def awaitAllStates: Future[Map[Waitable, Try[Unit]]] = {
    val serviseSeq = waitableServices.toSeq
    val classes = serviseSeq.map { _.getClass }
    whenAll(serviseSeq map { _.ready }) map { states =>
      classes.zip(states).toMap
    }
  }

  def awaitAllReady: Future[Unit] =
    whenAll(waitableServices.map(_.ready)).map(_ => ())

  def awaitReady[W : ClassTag](implicit ct: ClassTag[W]): Future[Unit] =
    waitableServices.find(ct.runtimeClass.isInstance).getOrThrow(s"Class $ct was not registered as ReadyStateService").ready

  override def addShutdownHook(hook: () => Future[_], name: String): Unit =
    registerOnTermination(hook, name)

}
