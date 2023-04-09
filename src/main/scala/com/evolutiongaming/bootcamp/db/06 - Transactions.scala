package com.evolutiongaming.bootcamp.db

import cats.effect._
import cats.implicits._
import doobie.implicits._

object Transactions extends IOApp {

  // CRUD: Create, Read, Update and Delete
  private val setup  = sql"CREATE TABLE crud (id INT AUTO_INCREMENT PRIMARY KEY, v VARCHAR)"
  private val count  = sql"SELECT id, v FROM crud"
  private val insert = sql"INSERT INTO crud(v) VALUES ('value')"

  private val xa = DbTransactor.make[IO] // or `pooled`

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- setup.update.run.transact(xa)
      _ <- printTable()
      _ <- insert.update.run.transact(xa)
      _ <- printTable()
      _ <- (
        insert.update.run.replicateA(5) *>
          WeakAsyncConnectionIO.raiseError(new Throwable("oops"))
      ).transact(xa)
        .handleErrorWith { e =>
          IO.delay(println(s"threw: $e"))
        }
      _ <- printTable()
      _ <- insert.update.run.transact(xa)
      _ <- printTable()
    } yield ExitCode.Success

  private def printTable(): IO[Unit] =
    count
      .query[(Int, String)]
      .to[List]
      .transact(xa)
      .map { l =>
        println("content of table:")
        l.foreach { case (k, v) => println(s"$k -> $v") }
        println("---")
      }
}
