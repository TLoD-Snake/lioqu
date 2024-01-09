package com.mysterria.lioqu.commons

/**
 * Wrap exceptions you throw inside the Future into this exception so that findAll skips it.
 */
class FindFirstSkippedException(inner: Throwable) extends Exception("Exception wrapped for skipping inside findFirst", inner)

object FindFirstSkippedException {
  def apply(inner: Throwable) = new FindFirstSkippedException(inner)
}