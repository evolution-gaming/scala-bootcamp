package com.evolutiongaming.bootcamp.db

import java.time.{LocalDate, Year}
import java.util.UUID

trait DbCommon {

  protected final val dbUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
  protected final val dbUser = ""
  protected final val dbPwd = ""
  protected final val dbDriverName = "org.h2.Driver"
  protected final val authorId1 = UUID.randomUUID()
  protected final val authorId2 = UUID.randomUUID()
  protected final val bookId1 = UUID.randomUUID()
  protected final val bookId2 = UUID.randomUUID()
  protected final val bookId3 = UUID.randomUUID()

  protected final val authorsSql =
    """CREATE TABLE authors (
      |  id UUID PRIMARY KEY,
      |  name VARCHAR(100) NOT NULL,
      |  birthday DATE);""".stripMargin
  protected final val booksSql =
    """CREATE TABLE books (
      |  id UUID PRIMARY KEY,
      |  author UUID NOT NULL,
      |  title VARCHAR(100) NOT NULL,
      |  year INT,
      |  FOREIGN KEY (author) REFERENCES authors(id));""".stripMargin
  protected final val populateDataSql =
    s"""
       |INSERT INTO authors (id, name, birthday) VALUES
       |  ('$authorId1', 'Martin Odersky', '1958-09-05'),
       |  ('$authorId2', 'J.K. Rowling', '1965-07-31');
       |
       |INSERT INTO books (id, author, title, year) VALUES
       |  ('$bookId1', '$authorId1', 'Programming in Scala', 2016),
       |  ('$bookId2', '$authorId2', 'Harry Potter and Philosopher''s Stone', 1997),
       |  ('$bookId3', '$authorId2', 'Harry Potter and the Chamber of Secrets', 1998);
       |""".stripMargin

  protected val fetchBooksCommonSql: String =
  """SELECT b.id, a.id, a.name, a.birthday, b.title, b.year FROM books b
      |INNER JOIN authors a ON b.author = a.id """.stripMargin
  protected val fetchHPBooksSql: String = fetchBooksCommonSql + s"WHERE b.author = '$authorId2';"

}
