package com.evolutiongaming.bootcamp.db

import _root_.munit._
import cats.effect.IO
import com.evolutiongaming.bootcamp.db.CustomMappings._
import com.evolutiongaming.bootcamp.db.DbConfig.{dbDriverName, dbPwd, dbUrl, dbUser}
import doobie._
import doobie.implicits._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class CustomMappingsSuite extends CatsEffectSuite with doobie.munit.IOChecker {
  implicit val executor: ExecutionContextExecutor = ExecutionContext.global

  val transactor = Transactor.fromDriverManager[IO](
    driver = dbDriverName,
    url = dbUrl,
    user = dbUser,
    pass = dbPwd,
  )

  test("select *") {
    setup().transact(transactor) *>
      IO.delay(check(selectAll()))
  }

}
