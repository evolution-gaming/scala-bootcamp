package com.evolutiongaming.bootcamp.db

import java.time.{LocalDate, Year}
import java.util.UUID

import cats.data.NonEmptyList
import cats.effect._
import doobie._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.h2._

object DoobieExercises extends IOApp with DbCommon {

  implicit val uuidMeta: Meta[UUID] = Meta[String].timap(UUID.fromString)(_.toString)
  implicit val yearMeta: Meta[Year] = Meta[Int].timap(Year.of)(_.getValue)

  override def run(args: List[String]): IO[ExitCode] =
    transactor.use(updateBook(bookId1, Year.of(2011)))

  private val transactor: Resource[IO, Transactor[IO]] =
    for {
      be <- Blocker[IO]
    } yield {
      Transactor.fromDriverManager[IO](
        url = dbUrl,
        user = dbUser,
        pass = dbPwd,
        blocker = be,
        driver = dbDriverName
      )
    }

  val simple: Transactor[IO] => IO[ExitCode] = xa =>
    for {
      n <- sql"select 42".query[Int].unique.transact(xa)
      _ <- IO(println(n))
    } yield ExitCode.Success

  val initTables: Transactor[IO] => IO[Int] = xa => {
    val authorsFr = Fragment.const(authorsSql)
    val booksFr = Fragment.const(booksSql)
    val initDataFr = Fragment.const(populateDataSql)
    (authorsFr ++ booksFr ++ initDataFr).update.run.transact(xa)
  }

  def fetchAuthor(id: UUID): Transactor[IO] => IO[ExitCode] = xa => {
    val queryAuthor = sql"SELECT id, name, birthday FROM authors WHERE id = $id;"
    for {
      _ <- initTables(xa)
      maybeAuthor <- queryAuthor.query[Author].option.transact(xa)
      _ <- IO(println(maybeAuthor))
    } yield ExitCode.Success
  }

  val fetchHPBooks: Transactor[IO] => IO[ExitCode] = xa => {
    val queryHPBooks = Fragment.const(fetchHPBooksSql)
    for {
      _ <- initTables(xa)
      books <- queryHPBooks.queryWithLogHandler[Book](LogHandler.jdkLogHandler).to[List].transact(xa)
      _ <- IO(books.foreach(println))
    } yield ExitCode.Success
  }

  def fetchBooksByAuthor(ids: NonEmptyList[UUID]): Transactor[IO] => IO[ExitCode] = xa => {
    val queryBooks = Fragment.const(fetchBooksCommonSql + "WHERE") ++ Fragments.in(fr"author", ids)
    for {
      _ <- initTables(xa)
      books <- queryBooks.queryWithLogHandler[Book](LogHandler.jdkLogHandler).to[List].transact(xa)
      _ <- IO(books.foreach(println))
    } yield ExitCode.Success
  }

  def fetchBookByYear(year: Int): Transactor[IO] => IO[ExitCode] = ???

  def fetchBookByYearRange(yearFrom: Int, yearTo: Int): Transactor[IO] => IO[ExitCode] = ???

  def insertAuthor(name: String, birthday: LocalDate): Transactor[IO] => IO[ExitCode] = xa => {
    val id = UUID.randomUUID()
    val insertAuthorQuery = sql"INSERT INTO authors (id, name, birthday) VALUES ($id, $name, $birthday);"
    val queryAuthors = sql"SELECT id, name, birthday FROM authors;"
    for {
      _ <- initTables(xa)
      _ <- insertAuthorQuery.update.run.transact(xa)
      authors <- queryAuthors.query[Author].to[List].transact(xa)
      _ <- IO(authors.foreach(println))
    } yield ExitCode.Success
  }

  def insertBook(title: String, authorId: UUID, year: Year): Transactor[IO] => IO[ExitCode] = ???

  def updateAuthor(id: UUID, name: String): Transactor[IO] => IO[ExitCode] = xa => {
    val updateAuthorQuery = sql"UPDATE authors SET name = $name WHERE id = $id;"
    val queryAuthor = sql"SELECT id, name, birthday FROM authors WHERE id = $id;"
    for {
      _ <- initTables(xa)
      _ <- updateAuthorQuery.update.run.transact(xa)
      author <- queryAuthor.query[Author].unique.transact(xa)
      _ <- IO(println(author))
    } yield ExitCode.Success
  }

  def updateBook(id: UUID, year: Year): Transactor[IO] => IO[ExitCode] = ???

}