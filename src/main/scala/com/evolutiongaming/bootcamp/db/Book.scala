package com.evolutiongaming.bootcamp.db

import java.time.Year
import java.util.UUID

final case class Book(id: UUID, author: Author, title: String, year: Year) {
  override def toString: String = s"$title ($year) by ${author.name}"
}
