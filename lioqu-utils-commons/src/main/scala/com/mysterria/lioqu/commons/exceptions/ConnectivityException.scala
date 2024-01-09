package com.mysterria.lioqu.commons.exceptions

class ConnectivityException(message: String, cause: Throwable) extends RuntimeException(message, cause)

class ConnectTimeoutException(message: String, cause: Throwable = null) extends ConnectivityException(message, cause)

class ClientConnectionWaitTimeoutException(message: String, cause: Throwable = null) extends ConnectivityException(message, cause)