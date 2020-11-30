package com.evolutiongaming.bootcamp.db

import java.util.UUID

object DbCommon {

  val authorOdersky: UUID = UUID.randomUUID()
  val authorRowling: UUID = UUID.randomUUID()
  val bookScala: UUID = UUID.randomUUID()
  val bookHPStone: UUID = UUID.randomUUID()
  val bookHPSecrets: UUID = UUID.randomUUID()

  val createTableAuthorsSql: String =
    """CREATE TABLE authors (
      |  id UUID PRIMARY KEY,
      |  name VARCHAR(100) NOT NULL,
      |  birthday DATE);""".stripMargin

  val createTableBooksSql: String =
    """CREATE TABLE books (
      |  id UUID PRIMARY KEY,
      |  author UUID NOT NULL,
      |  title VARCHAR(100) NOT NULL,
      |  year INT,
      |  FOREIGN KEY (author) REFERENCES authors(id));""".stripMargin

  val populateDataSql: String =
    s"""
       |INSERT INTO authors (id, name, birthday) VALUES
       |  ('$authorOdersky', 'Martin Odersky', '1958-09-05'),
       |  ('$authorRowling', 'J.K. Rowling', '1965-07-31');
       |
       |INSERT INTO books (id, author, title, year) VALUES
       |  ('$bookScala', '$authorOdersky', 'Programming in Scala', 2016),
       |  ('$bookHPStone', '$authorRowling', 'Harry Potter and Philosopher''s Stone', 1997),
       |  ('$bookHPSecrets', '$authorRowling', 'Harry Potter and the Chamber of Secrets', 1998);
       |""".stripMargin

  val fetchBooksCommonSql: String =
    """SELECT b.id, a.id, a.name, a.birthday, b.title, b.year FROM books b
      |INNER JOIN authors a ON b.author = a.id """.stripMargin

  val fetchHarryPotterBooksSql: String = fetchBooksCommonSql + s"WHERE b.author = '$authorRowling';"
}
