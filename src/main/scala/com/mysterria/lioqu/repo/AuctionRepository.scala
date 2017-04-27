package com.mysterria.lioqu.repo
import javax.inject.Inject

import com.mysterria.lioqu.connection.{DBComponent, PgDBConfigProvider}
import com.mysterria.lioqu.commons._
import com.typesafe.scalalogging.LazyLogging
import org.postgresql.jdbc.PgArray

import scala.concurrent.{ExecutionContext, Future}

trait AuctionRepository extends AuctionTable with LazyLogging { this: DBComponent =>
  protected implicit val ec: ExecutionContext
  import profile.api._
  def ddl = db.run {query.schema.create}

  /**
    * 1. Upsert auctions, collect IDs
    * 2. Mark all other non-deleted auctions as deleted and update deletedAt field
    */
  def updateAccountAuctions(accountId: Int, auctions: Seq[Auction]): Future[Unit] = {
    logger.debug("G2a Updating auctions")

    val dat = System.currentTimeMillis()
    val setDeletedQuery = query.filter(q => q.marketplaceAccountId === accountId)
      .map(q => (q.isDeleted, q.deletedAt))
      .update((true, Some(dat)))

    val updateActions = auctions map { _.copy(
      isDeleted = false,
      deletedAt = None,
      updatedAt = System.currentTimeMillis()
    )} map { a =>
      for {
        existing <- query.filter(ex => ex.marketplaceAccountId === a.marketplaceAccountId && ex.productId === a.productId).result.headOption
        _ = logger.debug(s"Auction ${a.name} upsert: existing = $existing")
        row = existing.map(ex => a.copy(id = ex.id, createdAt = ex.createdAt)).getOrElse( a.copy(createdAt = a.updatedAt))
        _ = logger.debug(s"Auction ${a.name} upsert: row to insert = $row")
        //result <- (query returning query).insertOrUpdate(row)
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

trait AuctionTable { this: DBComponent =>
  import profile.api._

  class AuctionTable(tag: Tag) extends Table[Auction](tag, schema, "auction") {
    val marketplaceId = column[Int]("marketplace_id")
    val marketplaceAccountId = column[Int]("marketplace_account_id")
    val productId = column[Int]("product_id")

    val imageUrl = column[String]("image_url")
    val name = column[String]("name")
    val platformProductId = column[Int]("platform_product_id")
    val sold = column[Int]("sold")
    val stock = column[Int]("stock")
    val reserved = column[Int]("reserved")
    val status = column[String]("status")
    val productType = column[String]("product_type")
    val period = column[Int]("period")
    val price = column[Double]("price")
    val currency = column[String]("currency")
    val lowestPrice = column[Double]("lowest_price")

    val isDeleted = column[Boolean]("is_deleted")
    val updatedAt = column[Long]("updated_at")
    val createdAt = column[Long]("created_at")
    val deletedAt = column[Option[Long]]("deleted_at")
    val id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def * = (marketplaceId, marketplaceAccountId, productId, imageUrl, name,
      platformProductId, sold, stock, reserved, status, productType,
      period, price, currency, lowestPrice, isDeleted, updatedAt, createdAt, deletedAt,
      id.?) <> (Auction.tupled, Auction.unapply)
  }

  protected val query = TableQuery[AuctionTable]
}

class AuctionRepositoryImpl @Inject()(protected implicit val ec: ExecutionContext) extends AuctionRepository with DBComponent

case class Auction(
  marketplaceId: Int,
  marketplaceAccountId: Int,
  productId: Int,

  imageUrl: String,
  name: String,
  platformProductId: Int,
  sold: Int,
  stock: Int,
  reserved: Int,
  status: String,
  productType: String,
  period: Int, // Days left
  price: Double,
  currency: String,
  lowestPrice: Double,

  isDeleted: Boolean = false,
  updatedAt: Long = 0,
  createdAt: Long = 0,
  deletedAt: Option[Long] = None,

  id: Option[Int] = None
)

