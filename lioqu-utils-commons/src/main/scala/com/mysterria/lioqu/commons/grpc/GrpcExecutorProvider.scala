package com.mysterria.lioqu.commons.grpc

import java.util.concurrent.{Executor, ExecutorService, Executors, ThreadFactory}

import akka.actor.ActorSystem
import GrpcAppThreadPool.GrpcAppThreadPool
import com.mysterria.lioqu.commons.service.ApplicationLifeCycle
import com.typesafe.scalalogging.LazyLogging
import javax.inject.{Inject, Provider}
import thesis._

import scala.concurrent.Future

abstract class GrpcExecutorProvider @Inject()(
  grpcAppThreadPool: GrpcAppThreadPool,
  grpcAppThreadLimit: Option[Int],
  as: ActorSystem,
  lifeCycle: ApplicationLifeCycle,
  threadFactoryBuilder: (String, Boolean) => ThreadFactory
) extends Provider[Option[Executor]] with LazyLogging {

  override final def get(): Option[Executor] = grpcAppThreadPool match {
    case GrpcAppThreadPool.Akka  =>
      logger.info(s"Using Akka ActorSystem dispatcher as a grpc thread pool")
      Some(as.dispatcher)
    case GrpcAppThreadPool.Fixed =>
      Some(fixedExecutor(grpcAppThreadLimit))
    case GrpcAppThreadPool.Default =>
      logger.info(s"Using default grpc thread pool (CachedThreadPool)")
      None
    case unknown => throw new RuntimeException(s"Can't handle grpc thread pool type '$unknown'")
  }

  private[grpc] def fixedExecutor(threadsRequested: Option[Int]): ExecutorService = {
    val threads = threadsRequested.filter(_ > 0).getOrElse(Runtime.getRuntime.availableProcessors())
    logger.info(s"Using fixed grpc thread pool with thread limit of $threads")
    con(Executors.newFixedThreadPool(threads, threadFactoryBuilder(s"grpc-app-thread-%d", true))) { executor =>
      lifeCycle.addShutdownHook(() => Future.successful(executor.shutdownNow()), "grpc-app-thread-pool")
    }
  }
}