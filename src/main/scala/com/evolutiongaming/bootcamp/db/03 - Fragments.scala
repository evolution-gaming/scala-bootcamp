package com.evolutiongaming.bootcamp.db

import java.time.Year
import java.util.UUID

import cats.data.NonEmptyList
import cats.effect._
import doobie._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.h2._
import com.evolutiongaming.bootcamp.db.DbCommon._

object FragmentsUsage extends IOApp {

  // TODO we will get to these in next step
  implicit val uuidMeta: Meta[UUID] = Meta[String].timap(UUID.fromString)(_.toString)
  implicit val yearMeta: Meta[Year] = Meta[Int].timap(Year.of)(_.getValue)

  override def run(args: List[String]): IO[ExitCode] = {
    val ddl1 = Fragment.const(createTableAuthorsSql)
    val ddl2 = Fragment.const(createTableBooksSql)
    val dml = Fragment.const(populateDataSql)

    val authors: Fragment =
      fr"SELECT id, name, birthday FROM authors"

    def authorById(id: UUID): doobie.Query0[Author] =
      (authors ++ fr"WHERE id = $id").query[Author]

    val fetchBooksAndAuthor: Fragment =
      fr"""SELECT b.id, a.id, a.name, a.birthday, b.title, b.year FROM books b
            INNER JOIN authors a ON b.author = a.id"""

    val HarryPotterBooks: doobie.Query0[Book] = {
//      val queryAllBooks = Fragment.const(
//        """SELECT b.id, a.id, a.name, a.birthday, b.title, b.year FROM books b
//            INNER JOIN authors a ON b.author = a.id WHERE b.author = '$authorId2';""".stripMargin,
//      )
      val queryHPBooks = fetchBooksAndAuthor ++ fr"WHERE b.author = $authorId2;"
      queryHPBooks.query[Book]
    }

//    def booksByAuthors(ids: NonEmptyList[UUID]): doobie.Query0[Book] = {
//      val queryBooks = fetchBooksAndAuthor ++ fr"WHERE" ++ Fragments.in(fr"author", ids)
//      queryBooks.query[Book]
//    }

    DbTransactor
      .make[IO]
      .use { xa =>
        for {
          // setup
          _ <- ddl1.update.run.transact(xa)
          _ <- ddl2.update.run.transact(xa)
          _ <- dml.update.run.transact(xa)

          // business part
          _ <- authorById(authorId1).option.transact(xa).map(println)
          _ <- authorById(UUID.randomUUID()).option.transact(xa).map(println)
          _ <- HarryPotterBooks.to[List].transact(xa).map(_.foreach(println))
//          _ <- booksByAuthors(NonEmptyList.of(authorId1, authorId2)).to[List].transact(xa).map(_.foreach(println))
//          _ <- {
//            val y = xa.yolo
//            import y._
//            booksByAuthors(NonEmptyList.of(authorId1, authorId2)).to[List].quick
//          }
        } yield ()
      }
      .as(ExitCode.Success)
  }
}
