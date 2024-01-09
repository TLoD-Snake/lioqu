package com.mysterria.lioqu.service

import com.mysterria.lioqu.commons.service.ApplicationLifeCycle
import java.io.IOException
import java.nio.file.{Files, Path}
import com.typesafe.config.Config
import javax.inject.Inject
import com.mysterria.lioqu.config.PidPath
import thesis._

import scala.concurrent.Future
import scala.util.Try

class PidService @Inject()(config: Config, lifeCycle: ApplicationLifeCycle) {
  val pathString: Option[String] =
    Option(System.getProperty(PidPath))
      .orElse {
        ifDef(config.hasPath(PidPath))(config.getString(PidPath))
      }


  val path: Option[Path] = pathString map { definedPathString =>
    val path = Path.of(definedPathString)

    if (Files.isDirectory(path))
      throw new IOException(s"Pid file '$path' is an existing directory. Please specify path to the pid file to be created.")

    val directoryPath = path.getParent
    if (Files.exists(directoryPath) && !Files.isDirectory(directoryPath))
      throw new IOException(s"Pid file directory '$directoryPath' is a regular file")

    Try(Files.createDirectories(directoryPath)).failed.foreach { cause =>
      throw new IOException(s"Unable to create pid file directory '$directoryPath': ${cause.getMessage}", cause)
    }

    Files.writeString(path, ProcessHandle.current().pid().toString)
  }

  def clean(): Unit = path foreach Files.delete

  lifeCycle.addShutdownHook(() => Future.successful(clean()), getClass.getSimpleName)
}
