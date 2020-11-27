package com.evolutiongaming.bootcamp.db

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import com.evolutiongaming.bootcamp.db.DbConfig._
import doobie.hikari.HikariTransactor

object ConnectionPool extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {

    val rng = sql"SELECT EXTRACT (EPOCH from CURRENT_TIMESTAMP()) * 1000".query[Long].unique

    // in one transaction
    transactor
      .use { xa =>
        rng.replicateA(55).transact(xa).map(println)
      }
      .as(ExitCode.Success)

    // TODO scenario for showing how transactions work

    // in many transactions
//    val timestamp = transactor
//      .use { xa =>
//        rng.transact(xa)
//      }
//    List
//      .fill(55)(timestamp)
//      .parSequence
////      .map(_.sorted)
//      .map(println)
//      .as(ExitCode.Success)
  }

  private val transactor: Resource[IO, Transactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](10)
      be <- Blocker[IO]
      xa <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = dbDriverName,
        url = dbUrl,
        user = dbUser,
        pass = dbPwd,
//        connectEC = ExecutionContexts.synchronous, // await connection on this EC
        connectEC = ce, // await connection on this EC
//        blocker = Blocker.liftExecutionContext(ExecutionContexts.synchronous), // execute JDBC operations on this EC
        blocker = be, // execute JDBC operations on this EC
      )
    } yield xa
}
