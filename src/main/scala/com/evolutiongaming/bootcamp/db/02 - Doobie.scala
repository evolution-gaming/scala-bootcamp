package com.evolutiongaming.bootcamp.db

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import shapeless._

import java.sql.SQLException

object Doobie extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    /** simplest possible `doobie` program */
    val rng: ConnectionIO[String] = "42".pure[ConnectionIO]

    /** simple queries, run query on DB, interpret resultset as stream, yield result */
//    val rng = sql"SELECT random()".query[Double].unique // expects exactly one line as response
//    val rng = sql"SELECT random()".query[Double].option // expects zero or one results
//    val rng = sql"SELECT random()".query[Double].nel // expects at least 1 result
//    val rng = sql"SELECT random()".query[Double].to[Vector] // accumulate results into collection

//    val rng = sql"SELECT random()".query[Double].stream // stream result

    /** transformation of result to complex structures */
//    val rng = sql"SELECT random(), 42, 'random roll'".query[(Double, Int, Option[String])].unique

//    type RS = (Double, Int, Option[String]) // #1
//    final case class RS(rng: Double, answer: Int, description: Option[String]) // #2
//    final case class R(rng: Double, answer: Int) // #3
//    final case class RS(result: R, description: Option[String]) // #3
//    type RS = Double :: Int :: Option[String] :: HNil // #4
//    import shapeless.record.Record // #5
//    type RS = Record.`'random -> Double, 'AoUQ -> Int, 'txt -> Option[String]`.T // #5
//    val rng = sql"SELECT random(), 42, 'random roll'".query[RS].unique

    /** composition of several queries in monadic style */
//    val rng: ConnectionIO[(Int, Double)] =
//      for {
//        a <- sql"SELECT 42".query[Int].unique
//        b <- sql"SELECT random()".query[Double].unique
//      } yield (a, b)

    /** composition of several queries in applicative style */
//    val rng: ConnectionIO[(Int, Double)] = {
//      val a = sql"SELECT 42".query[Int].unique
//      val b = sql"SELECT random()".query[Double].unique
//      (a, b).tupled
//    }

    /** `create`, `insert` and `update` */
//    val rng: doobie.ConnectionIO[(String, String)] = {
//      val create = sql"CREATE table kv(k VARCHAR(100) PRIMARY KEY, v VARCHAR(100))"
//      val key = "key"
//      val value = "value"
//      val insert = sql"INSERT INTO kv (k, v) VALUES ($key, $value)"
//      val newValue = "fixed value"
//      val update = sql"UPDATE kv SET v = $newValue WHERE k = $key"
//      val select = sql"SELECT k, v FROM kv"
//
//      create.update.run *>
//        insert.update.run *>
//        update.update.run *>
//        select.query[(String, String)].unique
//    }

    /** simple error handling */
//    val rng: doobie.ConnectionIO[Either[Throwable, String]] = "42".pure[ConnectionIO].attempt
//    val rng: doobie.ConnectionIO[Either[SQLException, String]] = sql"WRONG QUERY".query[String].unique.attemptSql

    val xa = DbTransactor.make[IO]
//      val rng2 = rng.replicateA(5)
    rng
      .transact(xa)
      .map(println)
      // streaming result
//      rng.take(3).compile.toList.transact(xa).map(println)
      .as(ExitCode.Success)
  }
}
