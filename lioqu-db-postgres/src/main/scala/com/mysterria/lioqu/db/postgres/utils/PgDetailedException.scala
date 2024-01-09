package com.mysterria.lioqu.db.postgres.utils

import org.postgresql.util.PSQLException

class PgDetailedException(cause: PSQLException, stack: Array[StackTraceElement]) extends Exception(
  s"Database error: ${cause.getServerErrorMessage}",
  cause
) {
  this.setStackTrace(stack)
}

object PgDetailedException {
  def apply(cause: PSQLException, stack: Array[StackTraceElement]) = new PgDetailedException(cause, stack)
}