package com.evolution.bootcamp.assignment.poker

final case class Board(
  cards: List[Card],
)

object Board {
  def of(x: String): Either[ErrorMessage, Board] = ???
}
