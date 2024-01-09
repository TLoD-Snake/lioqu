package com.mysterria.lioqu.commons.config

import com.typesafe.config.{Config, ConfigFactory}
import com.mysterria.lioqu.commons.config.TypesafeConfigHelper.TypesafeConfigExtensions
import org.scalatest.flatspec.AnyFlatSpec
import thesis._

class TypesafeConfigHelperTest extends AnyFlatSpec {
  "TypesafeConfigExtensions" should "work with sets" in {
    con(ConfigFactory.parseString("""roses: ["green", "red", "blue", "green"]""")) { config =>
      val roses: Set[String] = config.set("roses", _.getStringList)
      assert(roses.size == 3)

      val rosesOpt: Option[Set[String]] = config.setOpt("roses", _.getStringList)
      assert(rosesOpt.map(_.size).getOrElse(0) == 3)

      assert(rosesOpt.getOrElse(Set.empty) == roses)
    }

    con(ConfigFactory.empty()) { config =>
      val roses: Set[String] = config.set("roses", _.getStringList)
      assert(roses.isEmpty)

      val rosesOpt: Option[Set[String]] = config.setOpt("roses", _.getStringList)
      assert(rosesOpt.isEmpty)
    }

    con(ConfigFactory.parseString("""roses = [{tag: "flower"}, {tag: live}, {tag: immobile}]""")) { config =>
      val tags: Set[Config] = config.set("roses", _.getConfigList)
      assert(tags.size == 3)
      assert(tags.exists(_.getString("tag") == "flower"))
      assert(tags.exists(_.getString("tag") == "live"))
      assert(tags.exists(_.getString("tag") == "immobile"))
    }

    con(ConfigFactory.empty()) { config =>
      val tags: Set[Config] = config.set("roses", _.getConfigList)
      assert(tags.isEmpty)
    }
  }

  it should "work with seqs" in {
    con(ConfigFactory.parseString("""roses: ["green", "red", "blue", "green"]""")) { config =>
      val roses: Seq[String] = config.seq("roses", _.getStringList)
      assert(roses.size == 4)

      val rosesOpt: Option[Seq[String]] = config.seqOpt("roses", _.getStringList)
      assert(rosesOpt.map(_.size).getOrElse(0) == 4)

      assert(rosesOpt.getOrElse(Seq.empty) == roses)
    }

    con(ConfigFactory.empty()) { config =>
      val roses: Seq[String] = config.seq("roses", _.getStringList)
      assert(roses.isEmpty)

      val rosesOpt: Option[Seq[String]] = config.seqOpt("roses", _.getStringList)
      assert(rosesOpt.isEmpty)
    }

    con(ConfigFactory.parseString("""roses = [{tag: "flower"}, {tag: live}, {tag: immobile}]""")) { config =>
      val tags: Seq[Config] = config.seq("roses", _.getConfigList)
      assert(tags.size == 3)
      assert(tags.exists(_.getString("tag") == "flower"))
      assert(tags.exists(_.getString("tag") == "live"))
      assert(tags.exists(_.getString("tag") == "immobile"))
    }

    con(ConfigFactory.empty()) { config =>
      val tags: Seq[Config] = config.seq("roses", _.getConfigList)
      assert(tags.isEmpty)
    }
  }

  it should "retrieve primitive typed sets" in {
    val path = "years"
    val values = 2010 to 2013
    val config = ConfigFactory.parseString(s"""$path: [${values.mkString(",")}]""")

    {
      val longSetOpt = config.setOpt(path, _.getLongList)(Long2long)
      val longSet = config.set(path, _.getLongList)(Long2long)
      assert(longSet.size == values.size && longSet.forall(values.map(_.toLong).contains))
      assert(longSetOpt.get == longSet)
      assert(longSet == config.longSet(path))
      assert(longSet == config.longSetOpt(path).get)
    }

    {
      val intSetOpt = config.setOpt(path, _.getIntList)(Integer2int)
      val intSet = config.set(path, _.getIntList)(Integer2int)
      assert(intSet.size == values.size && intSet.forall(values.contains))
      assert(intSetOpt.get == intSet)
      assert(intSet == config.intSet(path))
      assert(intSet == config.intSetOpt(path).get)
    }

    {
      val doubleSetOpt = config.setOpt(path, _.getDoubleList)(Double2double)
      val doubleSet = config.set(path, _.getDoubleList)(Double2double)
      assert(doubleSet.size == values.size && doubleSet.forall(values.map(_.toDouble).contains))
      assert(doubleSetOpt.get == doubleSet)
      assert(doubleSet == config.doubleSet(path))
      assert(doubleSet == config.doubleSetOpt(path).get)
    }
    
  }

  it should "retrieve primitive typed sequences" in {
    val path = "years"
    val values = 2010 to 2013
    val config = ConfigFactory.parseString(s"""$path: [${values.mkString(",")}]""")

    {
      val longseqOpt = config.seqOpt(path, _.getLongList)(Long2long)
      val longseq = config.seq(path, _.getLongList)(Long2long)
      assert(longseq.size == values.size && longseq.forall(values.map(_.toLong).contains))
      assert(longseqOpt.get == longseq)
      assert(longseq == config.longSeq(path))
      assert(longseq == config.longSeqOpt(path).get)
    }

    {
      val intseqOpt = config.seqOpt(path, _.getIntList)(Integer2int)
      val intseq = config.seq(path, _.getIntList)(Integer2int)
      assert(intseq.size == values.size && intseq.forall(values.contains))
      assert(intseqOpt.get == intseq)
      assert(intseq == config.intSeq(path))
      assert(intseq == config.intSeqOpt(path).get)
    }

    {
      val doubleseqOpt = config.seqOpt(path, _.getDoubleList)(Double2double)
      val doubleseq = config.seq(path, _.getDoubleList)(Double2double)
      assert(doubleseq.size == values.size && doubleseq.forall(values.map(_.toDouble).contains))
      assert(doubleseqOpt.get == doubleseq)
      assert(doubleseq == config.doubleSeq(path))
      assert(doubleseq == config.doubleSeqOpt(path).get)
    }

  }

  it should "retrieve duration sets and seqs" in {
    import scala.concurrent.duration._
    val path = "key"
    val values = Seq(1 second, 1 minute, 1 hour)
    val config = ConfigFactory.parseString(s"""$path: [${values.mkString(",")}]""")

    {
      val durationSet: Set[FiniteDuration] = config.set(path, _.getDurationList)
      assert(durationSet.nonEmpty && durationSet.forall(values.contains))
      assert(config.setOpt(path, _.getDurationList)(castJavaDuration).get == durationSet)
    }

    {
      val durationSeq: Seq[FiniteDuration] = config.seq(path, _.getDurationList)
      assert(durationSeq.nonEmpty && durationSeq.forall(values.contains))
      assert(config.seqOpt(path, _.getDurationList)(castJavaDuration).get == durationSeq)
    }
  }

  it should "help with configs too" in {
    con(ConfigFactory.parseString("""cat = {voice: meow, scratch: true, cuteness: 100}""")) { config =>
      val catConfig: Config = config.config("cat")
      assert(!catConfig.isEmpty)
      assert(catConfig.bool("scratch"))
      assert(!catConfig.bool("bringsToys"))
      assert(catConfig.getInt("cuteness") == 100)

      val catConfigOpt = config.configOpt("cat")
      assert(catConfigOpt.getOrElse(ConfigFactory.empty()) == catConfig)
    }
  }

  it should "provide base functions" in {
    con(ConfigFactory.parseString("""voice: meow, scratch: true, cuteness: 100, identity: {name: Boris, occupation: cat}""")) { config =>
      assert(config.withDefault("name", _.getString, "Boris") == "Boris")
      assert(config.optional("name", _.getString).isEmpty)

      assert(config.withDefault("voice", _.getString, "Boris") == "meow")
      assert(config.optional("voice", _.getString).contains("meow"))
    }
  }
}
