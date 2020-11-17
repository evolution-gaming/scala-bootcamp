package com.evolutiongaming.bootcamp.db

import java.util.UUID

object DbCommon {

  val authorId1: UUID = UUID.randomUUID()
  val authorId2: UUID = UUID.randomUUID()
  val bookId1: UUID = UUID.randomUUID()
  val bookId2: UUID = UUID.randomUUID()
  val bookId3: UUID = UUID.randomUUID()

  val authorsSql: String =
    """CREATE TABLE authors (
      |  id UUID PRIMARY KEY,
      |  name VARCHAR(100) NOT NULL,
      |  birthday DATE);""".stripMargin

  val booksSql: String =
    """CREATE TABLE books (
      |  id UUID PRIMARY KEY,
      |  author UUID NOT NULL,
      |  title VARCHAR(100) NOT NULL,
      |  year INT,
      |  FOREIGN KEY (author) REFERENCES authors(id));""".stripMargin

  val populateDataSql: String =
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

  val fetchBooksCommonSql: String =
    """SELECT b.id, a.id, a.name, a.birthday, b.title, b.year FROM books b
      |INNER JOIN authors a ON b.author = a.id """.stripMargin

  val fetchHPBooksSql: String = fetchBooksCommonSql + s"WHERE b.author = '$authorId2';"
}
