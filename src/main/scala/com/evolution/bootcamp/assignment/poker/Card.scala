package com.evolution.bootcamp.assignment.poker

import cats.implicits._

final case class Card(
  rank: Rank,
  suit: Suit,
) {
  override def toString: ErrorMessage = s"$rank$suit"
}

object Card {
  def many(input: String): Either[ErrorMessage, List[Card]] = {
    if (input.length % 2 == 0) {
      (input.grouped(2).toList map Card.of).sequence
    } else {
      s"Invalid board $input".asLeft
    }
  }

  def of(x: String): Either[ErrorMessage, Card] = x.toList match {
    case r :: s :: Nil =>
      for {
        rank <- Rank.of(r)
        suit <- Suit.of(s)
      } yield Card(rank, suit)

    case _ => s"Failed to parse card $x".asLeft
  }
}
