package com.evolution.bootcamp.assignment.poker

import cats.implicits._

sealed trait Hand {
  def cards: List[Card]
  override def toString: ErrorMessage = cards.map(_.toString).mkString
}

object Hand {
  final case class Texas(cards: List[Card]) extends Hand
  final case class Omaha(cards: List[Card]) extends Hand

  def of(input: String): Either[ErrorMessage, Hand] = for {
    cards <- Card.many(input)
    hand  <-
      cards.length match {
        case 2 => Texas(cards).asRight
        case 4 => Omaha(cards).asRight
        case x => s"Invalid number of cards $x in $input".asLeft
      }
  } yield hand

  def many(input: List[String]): Either[ErrorMessage, List[Hand]] = {
    (input map of).sequence
  }
}
