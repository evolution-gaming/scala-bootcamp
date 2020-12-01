package com.evolutiongaming.bootcamp.db

import java.sql.{Connection, DriverManager}
import java.time.Year
import java.util.UUID

import com.evolutiongaming.bootcamp.db.DbCommon.{
  createTableAuthorsSql,
  createTableBooksSql,
  fetchHarryPotterBooksSql,
  populateDataSql,
}
import com.evolutiongaming.bootcamp.db.DbConfig.{dbDriverName, dbPwd, dbUrl, dbUser}

import scala.annotation.tailrec

object Jdbc {

  def main(args: Array[String]): Unit = {

    // just to make sure, that driver is loaded
    Class.forName(dbDriverName)
    // get a connection, it should be wrapped in try-catch or in `Resource` to make it safer
    val connection = DriverManager.getConnection(dbUrl, dbUser, dbPwd)

    // dirty and usually not required, data is already in DB
    setUpTables(connection)

    // direct transformation from corresponding, classic, Java code
    // can be made safer and nicer, but we will not do that here
    try {
      connection.setAutoCommit(false) // start transaction
      fetchHPBooks(connection).foreach(println)
      connection.commit() // commit transaction
    } finally try connection.close()
    catch {
      case e: Throwable =>
        println(s"there was an error: $e");
    }
  }

  private def setUpTables(connection: Connection): Unit = {
    val stmt = connection.createStatement()
    stmt.executeUpdate(createTableAuthorsSql)
    stmt.executeUpdate(createTableBooksSql)
    stmt.executeUpdate(populateDataSql)
    stmt.close()
  }

  private def fetchHPBooks(connection: Connection): List[BookWithAuthor] = {
    val stmt = connection.createStatement()
    val rs = stmt.executeQuery(fetchHarryPotterBooksSql)

    def parseBooks(): List[BookWithAuthor] = {
      @tailrec def internalParseBooks(acc: List[BookWithAuthor]): List[BookWithAuthor] =
        if (rs.next()) {
          val bookId = UUID.fromString(rs.getString("books.id"))
          val title = rs.getString("books.title")
          val year = Year.of(rs.getInt("books.year"))
          val author = Author(
            id = UUID.fromString(rs.getString("authors.id")),
            name = rs.getString("authors.name"),
            birthday = rs.getDate("authors.birthday").toLocalDate,
          )
          internalParseBooks(acc :+ BookWithAuthor(bookId, author, title, year))
        } else {
          acc
        }
      internalParseBooks(Nil)
    }

    val books = parseBooks()

    // at the end, have to close all resources!
    rs.close()
    stmt.close()
    books
  }
}
