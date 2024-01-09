package com.mysterria.lioqu.commons.logging

object LogHelpers {

  implicit class FormattedLogger(val logger: com.typesafe.scalalogging.Logger) extends AnyVal {
    def trace(message: FormattedLogMessage, throwable: Throwable = null): Unit = {
      log(
        format = message.format,
        args = message.args,
        throwable = throwable,
        call = (format, args) => logger.trace(format, args:_*)
      )
    }

    def debug(message: FormattedLogMessage, throwable: Throwable = null): Unit = {
      log(
        format = message.format,
        args = message.args,
        throwable = throwable,
        call = (format, args) => logger.debug(format, args:_*)
      )
    }

    def info(message: FormattedLogMessage, throwable: Throwable = null): Unit = {
      log(
        format = message.format,
        args = message.args,
        throwable = throwable,
        call = (format, args) => logger.info(format, args:_*)
      )
    }

    def warn(message: FormattedLogMessage, throwable: Throwable = null): Unit = {
      log(
        format = message.format,
        args = message.args,
        throwable = throwable,
        call = (format, args) => logger.warn(format, args:_*)
      )
    }

    def error(message: FormattedLogMessage, throwable: Throwable = null): Unit = {
      log(
        format = message.format,
        args = message.args,
        throwable = throwable,
        call = (format, args) => logger.error(format, args:_*)
      )
    }

    def log(format: String, args: Seq[AnyRef], throwable: Throwable = null, call: (String, Seq[AnyRef]) => Unit): Unit = {
      call(format, if (null != throwable) args :+ throwable else args)
    }
  }

  implicit class FormattedLogInterpolator(val sc: StringContext) extends AnyVal {
    def log(args: Any*): FormattedLogMessage = {
      val strings: Array[String] = sc.parts.toArray
      val format = strings.reduce((a, b) => {a + "{}" + b})
      FormattedLogMessage(format, args.map(_.asInstanceOf[AnyRef]))
    }
  }

  case class FormattedLogMessage(format: String, args: Seq[AnyRef]) {
    def +(other: FormattedLogMessage) = FormattedLogMessage(format + other.format, args ++ other.args)
  }
}
