package com.mysterria.lioqu.service

import javax.inject._
import akka.actor.ActorSystem
import com.mysterria.lioqu.di.ReadyStateService
import com.mysterria.lioqu.commons._
import com.typesafe.scalalogging.LazyLogging
import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.Try

class LifeCycleService @Inject()(
  as: ActorSystem,
  waitableServices: immutable.Set[ReadyStateService]
)(implicit ec: ExecutionContext) extends LazyLogging {
  type Waitable = Class[_ <: ReadyStateService]

  awaitAllStates foreach { states =>
    val statesStr = states.map(st => s"\t${st._1} => ${st._2.map{_=>"Success"}.getOrElse("Failure")}").mkString("\n")
    logger.info(s"All services done initilization:\n$statesStr")
  }

  as.registerOnTermination {
    logger.info("Bla-bla, shutting down")
    Thread.sleep(10000)
    logger.info("Bla-bla, ok, i'm done")
  }

  def awaitAllStates: Future[Map[Waitable, Try[Unit]]] = {
    val serviseSeq = waitableServices.toSeq
    val classes = serviseSeq.map { _.getClass }
    whenAll(serviseSeq map { _.ready }) map { states =>
      classes.zip(states).toMap
    }
  }

  def awaitAllReady: Future[Unit] = {
    whenAll(waitableServices.map(_.ready)).map(_ => ())
  }

  def awaitReady[W : ClassTag](implicit ct: ClassTag[W]): Future[Unit] = {
    waitableServices.find(ct.runtimeClass.isInstance).getOrThrow(s"Class $ct was not registered as ReadyStateService").ready
  }
}
