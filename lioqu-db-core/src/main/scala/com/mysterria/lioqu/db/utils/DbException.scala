package com.mysterria.lioqu.db.utils

class DbException(cause: Throwable, stack: Array[StackTraceElement]) extends Exception(
  cause.getMessage,
  cause
) {
  this.setStackTrace(stack)
}

object DbException {
  def apply(cause: Throwable, stack: Array[StackTraceElement]) = new DbException(cause, stack)
}
