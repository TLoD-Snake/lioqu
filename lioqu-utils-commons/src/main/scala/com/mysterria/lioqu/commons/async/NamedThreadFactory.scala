package com.mysterria.lioqu.commons.async

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import thesis._

/**
  * This ThreadFactory mimics behavior of Executors.DefaultThreadFactory used by default in SingleThreadExecutor
  * @param format thread name format, %d will be replaced with thread number
  */
class NamedThreadFactory(format: String) extends ThreadFactory {
  if (null == format) throw new NullPointerException("Format can not be null")
  format.format(0) // fail fast on incorrect format

  private val securityManager = System.getSecurityManager
  private val group = if (securityManager != null) securityManager.getThreadGroup else Thread.currentThread().getThreadGroup
  private val threadNumber = new AtomicInteger(1)

  override def newThread(r: Runnable): Thread = {
    con(new Thread(group, r, format.format(threadNumber.getAndIncrement), 0L)) { t =>
      if (t.isDaemon) t.setDaemon(false)
      if (t.getPriority != 5) t.setPriority(5)
    }
  }
}
