package com.evolutiongaming.bootcamp.db

import java.sql.{Connection, DriverManager}
import java.time.Year
import java.util.UUID

import com.evolutiongaming.bootcamp.db.DbConfig._
import com.evolutiongaming.bootcamp.db.DbCommon._

import scala.annotation.tailrec

object JdbcExample {

  def main(args: Array[String]): Unit = {
    Class.forName(dbDriverName)
    val connection = DriverManager.getConnection(dbUrl, dbUser, dbPwd)
    setUpTables(connection)
    fetchHPBooks(connection).foreach(println)
    connection.close()
  }

  private def setUpTables(connection: Connection): Unit = {
    val stmt = connection.createStatement()
    stmt.executeUpdate(authorsSql)
    stmt.executeUpdate(booksSql)
    stmt.executeUpdate(populateDataSql)
    stmt.close()
  }

  private def fetchHPBooks(connection: Connection): List[Book] = {
    val stmt = connection.createStatement()
    val rs = stmt.executeQuery(fetchHPBooksSql)
    def parseBooks(): List[Book] = {
      @tailrec def internalParseBooks(acc: List[Book]): List[Book] =
        if (rs.next()) {
          val bookId = UUID.fromString(rs.getString("books.id"))
          val title = rs.getString("books.title")
          val year = Year.of(rs.getInt("books.year"))
          val author = Author(
            id = UUID.fromString(rs.getString("authors.id")),
            name = rs.getString("authors.name"),
            birthday = rs.getDate("authors.birthday").toLocalDate,
          )
          internalParseBooks(acc :+ Book(bookId, author, title, year))
        } else {
          acc
        }
      internalParseBooks(List.empty)
    }
    val books = parseBooks()
    rs.close()
    stmt.close()
    books
  }
}
