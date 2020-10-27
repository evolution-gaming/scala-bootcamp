package com.evolution.bootcamp.assignment.poker

import cats.implicits._

final case class Board(
  cards: List[Card],
)

object Board {
  def of(input: String): Either[ErrorMessage, Board] = {
    Card.many(input)
      .bimap(
        x => s"Invalid board $input: $x",
        x => Board(x),
      )
  }
}
