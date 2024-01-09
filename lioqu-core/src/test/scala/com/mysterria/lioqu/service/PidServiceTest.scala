package com.mysterria.lioqu.service

import com.mysterria.lioqu.commons.service.ApplicationLifeCycle
import java.io.{File, IOException}
import java.nio.file.Files
import java.util.Scanner
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.JavaConverters._

class PidServiceTest extends AnyFlatSpec with MockitoSugar with LazyLogging {

  classOf[PidService].getSimpleName should "check pid file path prior to creation" in {
    val temp = Files.createTempDirectory("lioqu-test")
    logger.info(s"Created temp: $temp")

    checkPid(instance(s"${temp.toAbsolutePath.toString}${File.separator}pid"))

    {
      System.setProperty(com.mysterria.lioqu.config.PidPath, temp.resolve("envpid").toString)
      try {
        val pf = instance(s"${temp.toAbsolutePath.toString}${File.separator}pid")
        assert(pf.path.get.endsWith("envpid"))
        checkPid(pf)
      } finally {
        System.clearProperty(com.mysterria.lioqu.config.PidPath)
      }
    }

    checkPid(instance(s"${temp.toAbsolutePath.toString}${File.separator}nested${File.separator}pid"))

    assertThrows[IOException] {
      instance(s"${temp.toAbsolutePath.toString}${File.separator}pid")
      instance(s"${temp.toAbsolutePath.toString}${File.separator}pid${File.separator}pid2")
    }

    assertThrows[IOException] {
      instance(s"${temp.toAbsolutePath.toString}")
    }
  }

  def checkPid(ps: PidService): Unit = {
    ps.path match {
      case Some(path) =>
        val s = new Scanner(path)
        assert(s.hasNextLine)
        assert(ProcessHandle.current().pid() == s.nextLine().toLong)
      case None => throw new Exception("PidFile was not created for some reason. Check the test.")
    }
    ps.clean()
    assert(ps.path.exists(Files.notExists(_)))
  }


  def instance(pidPath: String): PidService = {
    val config = ConfigFactory.parseMap(Map(
      com.mysterria.lioqu.config.PidPath -> pidPath
    ).asJava)
    new PidService(config, mock[ApplicationLifeCycle])
  }

}
