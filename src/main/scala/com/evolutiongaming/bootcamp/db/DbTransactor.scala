package com.evolutiongaming.bootcamp.db

import cats.effect.{Async, Blocker, ContextShift, Resource}
import com.evolutiongaming.bootcamp.db.DbConfig.{dbDriverName, dbPwd, dbUrl, dbUser}
import doobie.Transactor

object DbTransactor {

  /** Simplest `transactor`, slow[er], inefficient for large apps, but OK for testing and learning.
    * Derives transactor from driver.
    */
  def make[F[_]: ContextShift: Async]: Resource[F, Transactor[F]] =
    Blocker[F].map { be =>
      Transactor.fromDriverManager[F](
        driver = dbDriverName,
        url = dbUrl,
        user = dbUser,
        pass = dbPwd,
        blocker = be,
      )
    }
}
