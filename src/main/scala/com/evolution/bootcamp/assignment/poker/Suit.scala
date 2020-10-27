package com.evolution.bootcamp.assignment.poker

import cats.syntax.either._

sealed trait Suit {
  def character: Char
  override def toString: ErrorMessage = character.toString
}

case object Hearts extends Suit {
  val character = 'h'
}

case object Diamonds extends Suit {
  val character = 'd'
}

case object Clubs extends Suit {
  val character = 'c'
}

case object Spades extends Suit {
  val character = 's'
}

object Suit {
  def of(x: Char): Either[ErrorMessage, Suit] = x match {
    case Hearts.character   => Hearts.asRight
    case Diamonds.character => Diamonds.asRight
    case Clubs.character    => Clubs.asRight
    case Spades.character   => Spades.asRight
    case _                  => s"Invalid char for suit: $x".asLeft
  }
}
