package com.evolutiongaming.bootcamp.db

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._

object Doobie extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    /** simplest possible `doobie` program */
    val rng: ConnectionIO[String] = "42".pure[ConnectionIO]

    /** simple queries */
//    val rng = sql"SELECT random()".query[Double].unique // expects exactly one line as response
//    val rng = sql"SELECT random()".query[Double].option // expects zero or one results
//    val rng = sql"SELECT random()".query[Double].nel // expects at least 1 result
//    val rng = sql"SELECT random()".query[Double].to[Vector] // accumulate results into collection

//    val rng = sql"SELECT random()".query[Double].stream // stream result

    /** transformation of result to complex structures */
//    val rng = sql"SELECT random(), 42, 'random roll'".query[(Double, Int, String)].unique

//    type RS = (Double, Int, String) // #1
//    final case class RS(rng: Double, answer: Int, description: String) // #2
//    final case class R(rng: Double, answer: Int) // #3
//    final case class RS(result: R, description: String) // #3
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
//      val create = sql"CREATE table kv(key VARCHAR(100) PRIMARY KEY, value VARCHAR(100))"
//      val key = "key"
//      val value = "value"
//      val insert = sql"INSERT INTO kv (key, value) VALUES ($key, $value)"
//      val newValue = "fixed value"
//      val update = sql"UPDATE kv SET value = $newValue WHERE key = $key"
//      val select = sql"SELECT key, value FROM kv"
//
//      create.update.run *>
//        insert.update.run *>
//        update.update.run *>
//        select.query[(String, String)].unique
//    }

    DbTransactor
      .make[IO]
      .use { xa =>
//        val rng2 = rng.replicateA(5)
        rng.transact(xa).map(println)
        // streaming result
//        rng.take(3).compile.toList.transact(xa).map(println)
      }
      .as(ExitCode.Success)
  }
}
