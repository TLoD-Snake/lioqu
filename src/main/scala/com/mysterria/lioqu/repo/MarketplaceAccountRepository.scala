package com.mysterria.lioqu.repo

import javax.inject.Inject

import com.mysterria.lioqu.connection.{DBComponent, PgDBConfigProvider}
import play.api.libs.json.JsValue

import scala.concurrent.{ExecutionContext, Future}


trait MarketplaceAccountRepository extends MarketplaceAccountTable { this: DBComponent =>
  implicit val ex: ExecutionContext
  import profile.api._

  def getEnabled(marketplaceId: Int): Future[Seq[MarketplaceAccount]] = db.run {
    query.filter(a => a.isEnabled === true && a.marketplaceId === marketplaceId).result
  }

  def ddl = db.run {query.schema.create}
}

trait MarketplaceAccountTable { this: DBComponent =>
  import profile.api._

  class MarketplaceAccountTable(tag: Tag) extends Table[MarketplaceAccount](tag, schema, "marketplace_account") {
    val name = column[String]("name")
    val marketplaceId = column[Int]("marketplace_id")
    val loginData = column[JsValue]("login_data")
    val isEnabled = column[Boolean]("is_enabled")
    val platformId = column[Int]("platform_id")
    val id = column[Int]("id", O.AutoInc, O.PrimaryKey)

    def * = (name, marketplaceId, loginData, isEnabled, platformId.?, id.?) <> (MarketplaceAccount.tupled, MarketplaceAccount.unapply)
  }

  protected val query = TableQuery[MarketplaceAccountTable]
}

class MarketplaceAccountRepositoryImpl @Inject()(implicit val ex: ExecutionContext)extends MarketplaceAccountRepository with DBComponent

case class MarketplaceAccount(
  name: String,
  marketplaceId: Int,
  loginData: JsValue,
  isEnabled: Boolean,
  platformId: Option[Int] = None,
  id: Option[Int] = None
) {

  override def toString: String = {
    s"$toStringSignature[$toStringNoSignature]"
  }

  def toStringNoSignature: String = s"$name(${id.getOrElse("None")})"

  def toStringSignature: String = getClass.getSimpleName + "@" + Integer.toHexString(hashCode)

}
