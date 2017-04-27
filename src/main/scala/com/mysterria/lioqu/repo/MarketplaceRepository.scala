package com.mysterria.lioqu.repo

import javax.inject.Inject

import com.mysterria.lioqu.connection.{DBComponent, PgDBConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

trait MarketplaceRepository extends MarketplaceTable { this: DBComponent =>
  protected implicit val ec: ExecutionContext
  import profile.api._

  def getByName(mpType: MarketplaceRepository.MarketplaceType.Value): Future[Option[Marketplace]] = {
    db.run(query.filter(_.name === mpType.toString).result.headOption)
  }

  def ddl = db.run {query.schema.create}

  def prefill: Future[Unit] = {
    val futures = MarketplaceRepository.MarketplaceType.values.map { mpt =>
      val q = sql"insert into marketplace (name) VALUES(${mpt.toString})"
      db.run(q.asUpdate)
    }
    Future.sequence(futures).map(_ => ())
  }
}

object MarketplaceRepository {
  object MarketplaceType extends Enumeration {
    val g2a = Value
  }
}

trait MarketplaceTable { this: DBComponent =>
  import profile.api._

  class MarketplaceTable(tag: Tag) extends Table[Marketplace](tag, schema, "marketplace") {
    val id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val name = column[String]("name", O.Unique)
    def * = (name, id.?) <> (Marketplace.tupled, Marketplace.unapply)
  }

  protected val query = TableQuery[MarketplaceTable]
}

class MarketplaceRepositoryImpl @Inject() (implicit protected val ec: ExecutionContext) extends MarketplaceRepository with DBComponent


case class Marketplace(name: String, id: Option[Int] = None)
