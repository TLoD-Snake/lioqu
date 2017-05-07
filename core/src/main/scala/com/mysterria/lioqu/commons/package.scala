package com.mysterria.lioqu

import java.util.concurrent.TimeUnit
import java.time.{Duration => JavaDuration}
import scala.concurrent.duration._
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.util.{Random, Try}
import scala.language.higherKinds
import scala.math.{BigDecimal, abs}

package object commons {

  /*
   * Great thanks to Igor K. and Slava K. for kindly provided scala common functions
   */

  def con[T, S](obj: T)(op: T => S) = {
    op(obj)
    obj
  }

  implicit def javaDurationToScala(javaDuration: JavaDuration): FiniteDuration =
    Duration(javaDuration.toMillis, TimeUnit.MILLISECONDS)

  implicit class FutureExtensions[T](f: Future[T]) {
    def mapAll[Target](m: Try[T] => Target)(implicit ec: ExecutionContext): Future[Target] = {
      val promise = Promise[Target]()
      f.onComplete { r =>
        try {
          promise success m(r)
        } catch {
          case err: Throwable => promise failure err
        }
      }(ec)
      promise.future
    }

    def mapAllEx[Target](m: Try[T] => Try[Target])(implicit ec: ExecutionContext): Future[Target] = {
      val promise = Promise[Target]()
      f.onComplete { r =>
        try {
          promise complete m(r)
        } catch {
          case err: Throwable => promise failure err
        }
      }(ec)
      promise.future
    }

    def flatMapAll[Target](m: Try[T] => Future[Target])(implicit ec: ExecutionContext): Future[Target] = {
      val promise = Promise[Target]()
      f.onComplete { r =>
        try {
          m(r).onComplete { z => promise complete z }(ec)
        } catch {
          case err: Throwable => promise failure err
        }
      }(ec)
      promise.future
    }
  }

  implicit class OptionExtensions[T](o: Option[T]) {
    def getOrThrow(message: => String): T  = {
      getOrThrowEx(new Exception(message))
    }

    def getOrThrowEx[E <: Throwable](exception: => E): T = {
      o.getOrElse {
        throw exception
      }
    }

    def getOrThrowRef[E <: Throwable](message: => String)(implicit ct: ClassTag[E]): T = {
      o.getOrElse {
        val constructor = ct.runtimeClass.getConstructor(classOf[String])
        val exception: Throwable = constructor.newInstance(message).asInstanceOf[Throwable]

        throw exception
      }
    }
  }

  def whenAll[A, M[X] <: TraversableOnce[X]](in: M[Future[A]])(implicit cbf: CanBuildFrom[M[Future[A]], Try[A], M[Try[A]]], executor: ExecutionContext): Future[M[Try[A]]] = {
    in.foldLeft(Future.successful(cbf(in))) {
      (fr, fa) =>
        fr flatMap { (r: mutable.Builder[Try[A], M[Try[A]]]) =>
          fa mapAll { (a: Try[A]) =>
            r += a
          }
        }
    } map (_.result())
  }

  /**
    * @return random delay between min and max.
    */
  def delay(min: FiniteDuration, max: FiniteDuration): FiniteDuration = {
    val diff = abs(max.toMillis - min.toMillis)
    (Seq(min, max).min.toMillis + BigDecimal(diff * Random.nextDouble).setScale(0, BigDecimal.RoundingMode.HALF_UP).toLong).millis
  }
}
