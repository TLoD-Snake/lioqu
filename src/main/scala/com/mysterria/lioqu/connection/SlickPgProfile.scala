package com.mysterria.lioqu.connection

import com.github.tminglei.slickpg.{ExPostgresProfile, PgDateSupportJoda, PgPlayJsonSupport}

/**
  * Minimal Slick 3.2 profile with Joda and Play Json
  */
class SlickPgProfile extends ExPostgresProfile
  with PgPlayJsonSupport
  with PgDateSupportJoda
{
  override def pgjson: String = "jsonb"

  override val api = PgSlickAPI

  object PgSlickAPI extends API
    with DateTimeImplicits
    with JodaDateTimePlainImplicits
    with JsonImplicits
    with PlayJsonPlainImplicits
}

object SlickPgProfile extends SlickPgProfile