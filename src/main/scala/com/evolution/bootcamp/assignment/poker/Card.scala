package com.evolution.bootcamp.assignment.poker

final case class Card(
  rank: Rank,
  suit: Suit,
)

object Card {
  def of(x: String): Either[ErrorMessage, Card] = ???
}
