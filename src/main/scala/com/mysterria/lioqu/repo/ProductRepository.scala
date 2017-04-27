package com.mysterria.lioqu.repo

import javax.inject._

import com.mysterria.lioqu.connection.DBComponent
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.{ExecutionContext, Future}
import com.mysterria.lioqu.commons._
import com.mysterria.lioqu.repo.{Product => LioquProduct}

trait ProductRepository extends ProductTable with  LazyLogging { this: DBComponent =>
  protected implicit val ec: ExecutionContext
  import profile.api._

  def ddl = db.run {query.schema.create}

  // TODO: Use Slick combinators here
  /**
    * @return Map[ MarketplaceProductID -> LioquProductID ]
    */
  def productMap(marketplaceId: Int, products: Seq[LioquProduct]): Future[Map[String, Int]] = {
    logger.debug(s"Reteieving productMap for products $products")
    val f = db.run { query.filter { product => product.platformId.inSetBind(products.map(_.platformId)) && product.marketplaceId === marketplaceId }.result } flatMap { existingProducts =>
      val notFound = products.map(_.platformId).diff(existingProducts.map(_.platformId))
      Future.sequence(
        products.filter(p => notFound.contains(p.platformId)).map { absentProduct =>
          db.run { insertQuery += absentProduct }
        }
      ) map { created =>
        (created ++ existingProducts).map{a => a.platformId -> a.id.get}.toMap
      }
    }
    con(f) { _.foreach { map =>
      logger.debug(s"ProductMap for [$products] is $map")
    }}
  }
}

trait ProductTable { this: DBComponent =>
  import profile.api._

  class ProductTable(tag: Tag) extends Table[Product](tag, schema, "product") {
    val marketplaceId = column[Int]("marketplace_id")
    val name = column[String]("name")
    val platformId = column[String]("platform_id")
    val id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def * = (marketplaceId, name, platformId, id.?) <> (Product.tupled, Product.unapply)
  }

  protected def query = TableQuery[ProductTable]

  protected def insertQuery = query returning query.map(_.id) into ((product, id) => product.copy(id = Some(id)))

  protected def autoInc = query returning query.map(_.id)
}

class ProductRepositoryImpl @Inject()(protected implicit val ec: ExecutionContext) extends ProductRepository with DBComponent

case class Product(marketplaceId: Int, name: String, platformId: String, id: Option[Int] = None)
