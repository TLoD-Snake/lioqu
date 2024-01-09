package com.mysterria.lioqu.db.postgres.utils

import com.mysterria.lioqu.db.postgres.utils.SlickPgDriver.api.actionBasedSQLInterpolation
import slick.jdbc.{PositionedParameters, SQLActionBuilder, SetParameter}

/**
  * Mix this trait into your DbServiceBase descendant to get the utils working.
  */
trait DbServiceUtils {
  implicit class SQLActionBuilderExtensions(ab: SQLActionBuilder) {
    /**
      * Allows concatenation of the two raw SQl queries like so (disregard backslashes):
      *   sql"select \$a, \$b" ++ sql", \$c, \$d ..."
      */
    def ++(other: SQLActionBuilder) = SQLActionBuilder(
      ab.queryParts ++ other.queryParts, new SetParameter[Unit] {
        override def apply(u: Unit, pp: PositionedParameters): Unit = {
          ab.unitPConv.apply(u, pp)
          other.unitPConv.apply(u, pp)
        }
      }
    )
  }

  implicit class SQLOptionExtensions[T](opt: Option[T]) {
    def clause(formatter: (T) => SQLActionBuilder, orElse: SQLActionBuilder = sql""): SQLActionBuilder = {
      opt map formatter getOrElse orElse
    }
  }

}
