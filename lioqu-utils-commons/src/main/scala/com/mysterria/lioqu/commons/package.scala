import akka.actor.ActorSystem
import akka.pattern.after
import com.mysterria.lioqu.commons.{Default, FindFirstSkippedException, TimeLoggingSettings}
import com.mysterria.lioqu.commons.logging.LogHelpers._
import com.typesafe.scalalogging.{LazyLogging, Logger}
import play.api.libs.json._

import java.util.NoSuchElementException
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicReference
import scala.jdk.CollectionConverters._
import scala.collection.BuildFrom
import scala.collection.mutable
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

package object thesis extends LazyLogging {

  def con[T, S](obj: T)(op: T => S) = {
    op(obj)
    obj
  }

  @deprecated("Use builtin Scala ARM", "2.13.0")
  def using[T <: { def close(): Unit }, S](obj: T)(op: T => S): S = {
    try {
      op(obj)
    } finally {
      obj.close()
    }
  }

  def ifDef[T](cond: Boolean)(v: => T): Option[T] =
    if (cond) Some(v) else None

  // Future utils ---

  def whenAll[A, M[X] <: IterableOnce[X]](in: M[Future[A]])(implicit cbf: BuildFrom[M[Future[A]], Try[A], M[Try[A]]], executor: ExecutionContext): Future[M[Try[A]]] = {
    in.iterator.foldLeft(Future.successful(cbf.newBuilder(in))) {
      (fr, fa) =>
        fr flatMap { (r: mutable.Builder[Try[A], M[Try[A]]]) =>
          fa mapAll { (a: Try[A]) =>
            r += a
          }
        }
    } map (_.result())
  }

  // other utils

  def __log_time__(start: Long, inf: => String)(implicit logger: Logger, settings: TimeLoggingSettings) = {
    import concurrent.duration._
    val fd = Duration(System.currentTimeMillis - start, MILLISECONDS)
    val msg = log"$inf took $fd"
    fd match {
      case d if d < settings.traceTreshold.millis => logger.trace(msg)
      case d if d < settings.debugThreshold.millis => logger.debug(msg)
      case d if d < settings.infoThreshold.millis => logger.info(msg)
      case d if d < settings.warnThreshold.millis => logger.warn(msg)
      case _ => logger.error(msg)
    }
  }

  def findFirst[A, R](seq: Seq[A])
                     (futureGen: A => Future[R])
                     (predicate: R => Boolean = (_: R) => true)
                     (implicit ec: ExecutionContext): Future[R] = {
    val iterator: Iterator[A] = seq.iterator

    findFirstIter(iterator)(futureGen)(predicate)
  }

  def findFirstIter[A, R](iterator: Iterator[A])
                         (futureGen: A => Future[R])
                         (predicate: R => Boolean = (_: R) => true)
                         (implicit ec: ExecutionContext): Future[R] = {
    if (iterator.hasNext) {
      futureGen(iterator.next()).filter(predicate).recoverWith {
        case noElement: NoSuchElementException =>
          findFirstIter(iterator)(futureGen)(predicate)

        case skipped: FindFirstSkippedException =>
          findFirstIter(iterator)(futureGen)(predicate)

        case err => Future.failed[R](err)
      }
    } else {
      Future.failed[R](new NoSuchElementException)
    }
  }

  // ===================================================================================================================
  // pimp-my-library

  implicit class TernaryBoolean(condition: Boolean) {
    @inline final def ?[T](trueVal: =>T): JustTrue[T] =
      new JustTrue(condition, trueVal)

    final class JustTrue[T](cond: Boolean, trueVal: =>T) {
      @inline def |(falseVal: =>T): T =
        if (cond) trueVal else falseVal
    }
  }

  implicit class FutureExtensions[T](f: Future[T]) {
    def mapAll[Target](m: Try[T] => Target)(implicit ec: ExecutionContext): Future[Target] = mapAllEx(x => Success(m(x)))

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

    def logError(logger: Logger, message: FormattedLogMessage)(implicit ec: ExecutionContext) =
      con(f)(_.failed foreach(logger.error(message, _)))

    def blocking()(implicit atMost: Duration): Unit = {
      Await.ready(f, atMost)
    }

    def withTimeout(duration: FiniteDuration, timeout: => Throwable)(implicit system: ActorSystem, ec: ExecutionContext): Future[T] = {
      Future firstCompletedOf Seq(f, after(duration, system.scheduler)(Future.failed(timeout)))
    }
  }

  implicit class TryFutureExtensions[A, M[X] <: IterableOnce[X]](f: Future[M[Try[A]]]) {
    def logTryErrors(logger: Logger, singleOp: => String, cumulativeOp: => String)(implicit ec: ExecutionContext): Unit = {
      f andThen {
        case Success(results) =>
          results.logTryErrors(logger, singleOp)
        case Failure(err) =>
          logger.error(log"Error $cumulativeOp", err)
      }
    }
  }

  implicit class TryCollectionExtensions[A, M[X] <: IterableOnce[X]](c: M[Try[A]]) {
    def logTryErrors(logger: Logger, singleOp: => String): Unit = {
      c.iterator.foreach {
        case Success(_) =>
        case Failure(err) =>
          logger.error(log"Error $singleOp", err)
      }
    }
  }

  implicit class FutureCollectionExtensions[A, M[X] <: IterableOnce[X]](f: M[Future[A]]) {
    def logErrors(logger: Logger, singleOp: => String)(implicit cbf: BuildFrom[M[Future[A]], Try[A], M[Try[A]]], executor: ExecutionContext): Unit = {
      whenAll(f).logTryErrors(logger, singleOp, "Error in whenAll implementation")
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

  implicit class MapExtensions[K, +V](map: scala.collection.Map[K, V]) {
    def getOrThrow(key: K, message: => String): V  = {
      getOrThrowEx(key, new Exception(message))
    }

    def getOrThrowEx[E <: Throwable](key: K, exception: => E): V = {
      map.get(key).getOrThrowEx(exception)
    }

    def getOrThrowRef[E <: Throwable](key: K, message: => String)(implicit ct: ClassTag[E]): V = {
      map.get(key).getOrThrowRef[E](message)
    }
  }

  implicit class IteratorExtensions[A](iter: Iterator[A]) {
    def nextOption() = ifDef(iter.hasNext)(iter.next())
  }

  implicit class SemaphoreExtensions(l: Semaphore) {
    def sync[T](op: => T): T = {
      l.acquire()

      try {
        op
      } finally {
        l.release()
      }
    }

    def futureSync[T](op: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
      l.acquire()

      val result = try {
        op
      } catch {
        case err: Throwable =>
          l.release()

          throw err
      }

      result onComplete { _ =>
        l.release()
      }

      result
    }
  }

  implicit class FunctionExtension[K, V](value: (K) => V) extends AnyRef {
    def asJava: java.util.function.Function[K, V] = {
      new java.util.function.Function[K, V] {
        override def apply(t: K): V = value(t)
      }
    }
  }

  implicit class BiFunctionExtension[A1, A2, V](value: (A1, A2) => V) extends AnyRef {
    def asJava: java.util.function.BiFunction[A1, A2, V] = {
      new java.util.function.BiFunction[A1, A2, V] {
        override def apply(a1: A1, a2: A2): V = value(a1, a2)
      }
    }
  }

  implicit class ConcurrentHashMapExtension[K, V >: Null](value: scala.collection.concurrent.Map[K, V]) extends AnyRef {
    def computeIfAbsent(key: K, valueGen: (K) => V): V = {
      value.asJava.computeIfAbsent(key, valueGen.asJava)
    }

    def computeIfPresent(key: K, valueGen: (K, V) => V): V = {
      value.asJava.computeIfPresent(key, valueGen.asJava)
    }

    def compute(key: K, valueGen: (K, V) => V): V = {
      value.asJava.compute(key, valueGen.asJava)
    }

    def computeOptNoKey(key: K, valueGen: Option[V] => Option[V]): V = {
      val update = new java.util.function.BiFunction[K, V, V] {
        override def apply(k: K, v: V): V = valueGen(Option(v)).orNull
      }
      value.asJava.compute(key, update)
    }

    final def casUpdate(key: K, valueGen: V => V, newValueGen: K => V): V = {
      val update = new java.util.function.BiFunction[K, V, V] {
        override def apply(k: K, v: V): V = {
          if(v == null) newValueGen(k)
          else valueGen(v)
        }
      }

      value.asJava.compute(key, update)
    }
  }

  implicit class AtomicReferenceExtensions[T](reference: AtomicReference[T]) {
    def casUpdate(newValueGen: T => T)(implicit default: Default[T]): T = {
      var prevValue: T = default.value
      var newValue: T = default.value
      do {
        prevValue = reference.get()
        newValue = newValueGen(prevValue)
      } while (
        !reference.compareAndSet(prevValue, newValue)
      ) // CAS

      newValue
    }

    def casUpdateLog(newValueGen: T => T, source: String)(implicit default: Default[T]): T = {
      var prevValue: T = default.value
      var newValue: T = default.value
      var retries = 0
      do {
        prevValue = reference.get()
        newValue = newValueGen(prevValue)
        retries += 1
      } while (
        !reference.compareAndSet(prevValue, newValue)
      ) // CAS

      if(retries > 1000) logger.error(log"CAS update ($source) took $retries number of tries to complete for $reference!")
      else if (retries > 100) logger.warn(log"CAS update ($source) took $retries number of tries to complete for $reference!")

      newValue
    }
  }

  implicit class EnumerationExtensions[E <: Enumeration](e: E) {
    final def withNameIgnoreCase(s: String): E#Value =
      e.values.find(_.toString.toUpperCase == s.toUpperCase).getOrThrowRef[NoSuchElementException](s"Enumeration $e has no value for '$s'")
  }

  object DoubleExtensions {
    val cacheUpToDigits = 10
    val digitsMultiply: Seq[Double] = for (i <- 0 to cacheUpToDigits) yield math.pow(10,  i)
  }

  implicit class DoubleExtensions(d: Double) {
    def noGarbage = {
      Math.round(d * 1e11) / 1e11
    }

    def currency2pt = {
      Math.round(d * 1e2) / 1e2
    }

    /**
      * Rounds number with the defined decimal precision
      */
    def roundAt(precision: Int): Double = {
      val multiplier = (precision <= DoubleExtensions.cacheUpToDigits) ? DoubleExtensions.digitsMultiply(precision) | math.pow(10, precision)
      math.round(d * multiplier) / multiplier
    }
  }

  /**
    * JSON ENSURE FIELDS
    *
    * Taken from http://stackoverflow.com/a/28903663/579817
    */
  implicit class WritesOps[A](val self: Writes[A]) {
    def ensureField(fieldName: String, path: JsPath = __, value: JsValue = JsNull): Writes[A] = {
      val update = path.json.update(
        __.read[JsObject].map( o => if(o.keys.contains(fieldName)) o else o ++ Json.obj(fieldName -> value))
      )
      self.transform(js => js.validate(update) match {
        case JsSuccess(v,_) => v
        case err: JsError => throw JsResultException(err.errors)
      })
    }

    def ensureFields(fieldNames: String*)(value: JsValue = JsNull, path: JsPath = __): Writes[A] =
      fieldNames.foldLeft(self)((w, fn) => w.ensureField(fn, path, value))
  }

  // ===================================================================================================================
  // implicit converters

  implicit def castJavaDuration(from: java.time.Duration): concurrent.duration.FiniteDuration =
    concurrent.duration.Duration.fromNanos(from.toNanos)

  object Implicits {
    implicit lazy val defaultTimeLoggingSettings: TimeLoggingSettings = TimeLoggingSettings()
  }
}
