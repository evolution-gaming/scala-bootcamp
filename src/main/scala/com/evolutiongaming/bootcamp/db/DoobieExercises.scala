package com.evolutiongaming.bootcamp.db

import java.time.{LocalDate, Year}
import java.util.UUID

import cats.data.NonEmptyList
import cats._
import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.h2._

import com.evolutiongaming.bootcamp.db.DbConfig._
import com.evolutiongaming.bootcamp.db.DbCommon._

object DoobieExercises extends IOApp {

  implicit val uuidMeta: Meta[UUID] = Meta[String].timap(UUID.fromString)(_.toString)
  implicit val yearMeta: Meta[Year] = Meta[Int].timap(Year.of)(_.getValue)

  override def run(args: List[String]): IO[ExitCode] =
    transactor
      .use { xa =>
        sql"select random()".query[Double].unique.replicateA(5).transact(xa).map(println)
//        exercise00
//        simple
//        updateAuthor(authorId1, "xxx")
//        fetchBooksByAuthor(NonEmptyList.of(authorId1))
//        fetchHPBooks
//        updateBook(bookId1, Year.of(2011))
      }
      .as(ExitCode.Success)

  private val transactor: Resource[IO, Transactor[IO]] =
    for {
      be <- Blocker[IO]
    } yield Transactor.fromDriverManager[IO](
      driver = dbDriverName,
      url = dbUrl,
      user = dbUser,
      pass = dbPwd,
      blocker = be,
    )

//  // better return `ConnectionIO`, per FAQ
//  val exercise00: Transactor[IO] => IO[Unit] = xa =>
//    for {
//      n <- 42.pure[ConnectionIO].transact(xa)
//      _ <- IO(println(n))
//    } yield ()
//
//  val simple: Transactor[IO] => IO[Unit] = xa =>
//    for {
//      n <- sql"select 42".query[Int].unique.transact(xa)
//      _ <- IO(println(n))
//    } yield ()

  val initTables: Transactor[IO] => IO[Int] = xa => {
    val authorsFr = Fragment.const(createTableAuthorsSql)
    val booksFr = Fragment.const(createTableBooksSql)
    val initDataFr = Fragment.const(populateDataSql)
    (authorsFr ++ booksFr ++ initDataFr).update.run.transact(xa)
  }

//  def fetchAuthor(id: UUID): Transactor[IO] => IO[Unit] = xa => {
//    val queryAuthor = sql"SELECT id, name, birthday FROM authors WHERE id = $id;"
//    for {
//      _ <- initTables(xa)
//      maybeAuthor <- queryAuthor.query[Author].option.transact(xa)
//      _ <- IO(println(maybeAuthor))
//    } yield ()
//  }

//  val fetchHPBooks: Transactor[IO] => IO[Unit] = xa => {
//    val queryHPBooks = Fragment.const(fetchHarryPotterBooksSql)
//    for {
//      _ <- initTables(xa)
//      books <- queryHPBooks.queryWithLogHandler[Book](LogHandler.jdkLogHandler).to[List].transact(xa)
//      _ <- IO(books.foreach(println))
//    } yield ()
//  }
//
//  def fetchBooksByAuthor(ids: NonEmptyList[UUID]): Transactor[IO] => IO[Unit] = xa => {
//    val queryBooks = Fragment.const(fetchBooksCommonSql + "WHERE") ++ Fragments.in(fr"author", ids)
//    for {
//      _ <- initTables(xa)
//      books <- queryBooks.queryWithLogHandler[Book](LogHandler.jdkLogHandler).to[List].transact(xa)
//      _ <- IO(books.foreach(println))
//    } yield ()
//  }

  def fetchBookByYear(year: Int): Transactor[IO] => IO[Unit] = ???

  def fetchBookByYearRange(yearFrom: Int, yearTo: Int): Transactor[IO] => IO[Unit] = ???

  def insertAuthor(name: String, birthday: LocalDate): Transactor[IO] => IO[Unit] = xa => {
    val queryAuthors = sql"SELECT id, name, birthday FROM authors;"
    for {
      id <- IO(UUID.randomUUID())
      insertAuthorQuery = sql"INSERT INTO authors (id, name, birthday) VALUES ($id, $name, $birthday);"
      _ <- initTables(xa)
      _ <- insertAuthorQuery.update.run.transact(xa)
      authors <- queryAuthors.query[Author].to[List].transact(xa)
      _ <- IO(authors.foreach(println))
    } yield ()
  }

  def insertBook(title: String, authorId: UUID, year: Year): Transactor[IO] => IO[Unit] = ???

  def updateAuthor(id: UUID, name: String): Transactor[IO] => IO[Unit] = xa => {
    val updateAuthorQuery = sql"UPDATE authors SET name = $name WHERE id = $id;"
    val queryAuthor = sql"SELECT id, name, birthday FROM authors WHERE id = $id;"
    for {
      _ <- initTables(xa)
      _ <- updateAuthorQuery.update.run.transact(xa)
      author <- queryAuthor.query[Author].unique.transact(xa)
      _ <- IO(println(author))
    } yield ()
  }

  def updateBook(id: UUID, year: Year): Transactor[IO] => IO[Unit] = ???

}
