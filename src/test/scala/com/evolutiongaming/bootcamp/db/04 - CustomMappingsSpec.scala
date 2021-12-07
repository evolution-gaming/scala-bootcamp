package com.evolutiongaming.bootcamp.db

import _root_.munit._
import cats.implicits._
import cats.effect.{ContextShift, IO}
import com.evolutiongaming.bootcamp.db.DbConfig.{dbDriverName, dbPwd, dbUrl, dbUser}
import doobie._
import doobie.implicits._
import CustomMappings._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class CustomMappingsSuite extends CatsEffectSuite with doobie.munit.IOChecker {
  implicit val executor: ExecutionContextExecutor = ExecutionContext.global
  implicit val contextShiftIO: ContextShift[IO] = IO.contextShift(executor)

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