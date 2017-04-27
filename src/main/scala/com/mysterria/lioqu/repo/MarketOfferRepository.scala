package com.mysterria.lioqu.repo

import javax.inject.Inject
import com.mysterria.lioqu.connection.DBComponent
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.{ExecutionContext, Future}

trait MarketOfferRepository extends MarketOfferTable with LazyLogging { this: DBComponent =>
  protected implicit val ec: ExecutionContext
  import profile.api._

  def ddl = db.run {query.schema.create}

  def updateMarketOffers(productId: Int, offers: Seq[MarketOffer]): Future[Unit] = {
    val now = System.currentTimeMillis()

    val setDeletedQuery = query.filter(q => q.productId === productId)
      .map(q => (q.isDeleted, q.deletedAt))
      .update((true, Some(now)))

    val updateActions = offers map { _.copy(
      isDeleted = false,
      deletedAt = None,
      updatedAt = now
    )} map { offer =>
      for {
        existing <- query.filter(ex => ex.productId === offer.productId && ex.seller === offer.seller).result.headOption
          _ = logger.debug(s"MarketOffer ${offer.seller} upsert: existing = $existing")
        row = existing.map(ex => offer.copy(id = ex.id, createdAt = ex.createdAt)).getOrElse( offer.copy(createdAt = now))
          _ = logger.debug(s"MarketOffer ${offer.seller} upsert: row to insert = $row")
        result <- query.insertOrUpdate(row)
      } yield ()
    }

    val action = (for {
      _ <- setDeletedQuery
      _ <- DBIO.seq(updateActions: _*)
    } yield()).transactionally

    db.run(action)
  }
}

trait MarketOfferTable { this: DBComponent =>
  import profile.api._

  class MarketOfferTable(tag: Tag) extends Table[MarketOffer](tag, schema, "market_offer") {
    val marketplaceId = column[Int]("marketplace_id")
    val productId = column[Int]("product_id")
    val seller = column[String]("seller")
    val price = column[Double]("price")
    val currency = column[String]("currency")

    val isDeleted = column[Boolean]("is_deleted")
    val updatedAt = column[Long]("updated_at")
    val createdAt = column[Long]("created_at")
    val deletedAt = column[Option[Long]]("deleted_at")
    val id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def * = (marketplaceId, productId, seller, price, currency, isDeleted, updatedAt, createdAt, deletedAt,
      id.?) <> (MarketOffer.tupled, MarketOffer.unapply)
  }

  protected def query = TableQuery[MarketOfferTable]
}

class MarketOfferRepositoryImpl @Inject()(implicit protected val ec: ExecutionContext) extends MarketOfferRepository with DBComponent

case class MarketOffer(
  marketplaceId: Int,
  productId: Int,
  seller: String,
  price: Double,
  currency: String,

  isDeleted: Boolean = false,
  createdAt: Long = 0,
  updatedAt: Long = 0,
  deletedAt: Option[Long] = None,
  id: Option[Int] = None
)
