package com.evolutiongaming.bootcamp.db

import java.time.Year
import java.util.UUID

import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.h2._
import com.evolutiongaming.bootcamp.db.DbCommon._

object FragmentsUsage extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    DbTransactor
      .make[IO]
      .use { xa =>
        for {
          // setup
          _ <- setup().transact(xa)

          // business part
          _ <- fetchAuthorById(authorOdersky).option.transact(xa).map(println)
//          _ <- fetchAuthorById(UUID.randomUUID()).option.transact(xa).map(println)
//          _ <- fetchHarryPotterBooks.to[List].transact(xa).map(_.foreach(println))
//          _ <- fetchBooksByAuthors(NonEmptyList.of(authorOdersky, authorRowling))
//            .to[List]
//            .transact(xa)
//            .map(_.foreach(println))
//          _ <- fetchBooksByYear(1998).transact(xa).map(_.foreach(println))
//          _ <- fetchBooksByYearRange(1997, 2001).transact(xa).map(_.foreach(println))
//          _ <-
//            (insertBook("Harry Potter and the Cursed Child - Parts I & II", authorRowling, Year.of(2016)) *>
//              fetchBooksByAuthors(NonEmptyList.of(authorRowling)).to[List])
//              .transact(xa)
//              .map(_.foreach(println))
//          _ <- updateYearOfBook(bookHPStone, Year.of(2003)).transact(xa)
        } yield ()
      }
      .as(ExitCode.Success)

  implicit val uuidMeta: Meta[UUID] = Meta[String].timap(UUID.fromString)(_.toString)
  implicit val yearMeta: Meta[Year] = Meta[Int].timap(Year.of)(_.getValue)

  // setup
  val ddl1 = Fragment.const(createTableAuthorsSql)
  val ddl2 = Fragment.const(createTableBooksSql)
  val dml = Fragment.const(populateDataSql)

  def setup(): ConnectionIO[Unit] =
    for {
      _ <- ddl1.update.run
      _ <- ddl2.update.run
      _ <- dml.update.run
    } yield ()

  val authors: Fragment =
    fr"SELECT id, name, birthday FROM authors"

  val books: Fragment =
    fr"SELECT id, author, title, year FROM books"

  def fetchAuthorById(id: UUID): doobie.Query0[Author] =
    (authors ++ fr"WHERE id = $id").query[Author]

  val fetchBooksAndAuthor: Fragment =
    fr"""SELECT b.id, a.id, a.name, a.birthday, b.title, b.year FROM books b
            INNER JOIN authors a ON b.author = a.id"""

  val fetchHarryPotterBooks: doobie.Query0[BookWithAuthor] = {
//    val queryAllBooks = Fragment.const(
//      """SELECT b.id, a.id, a.name, a.birthday, b.title, b.year FROM books b
//          INNER JOIN authors a ON b.author = a.id WHERE b.author = '$authorId2';""".stripMargin,
//    )
    val queryHPBooks = fetchBooksAndAuthor ++ fr"WHERE b.author = $authorRowling;"
    queryHPBooks.query[BookWithAuthor]
  }

  def fetchBooksByAuthors(ids: NonEmptyList[UUID]): doobie.Query0[BookWithAuthor] = {
    val queryBooks = fetchBooksAndAuthor ++ fr"WHERE" ++ Fragments.in(fr"author", ids)
    queryBooks.query[BookWithAuthor]
  }

  def fetchBooksByYear(year: Int): doobie.ConnectionIO[List[Book]] = ???

  def fetchBooksByYearRange(yearFrom: Int, yearTo: Int): doobie.ConnectionIO[List[Book]] = ???

  def insertBook(title: String, authorId: UUID, year: Year): doobie.ConnectionIO[Int] = ???

  def updateYearOfBook(id: UUID, year: Year): doobie.ConnectionIO[Int] = ???
}
