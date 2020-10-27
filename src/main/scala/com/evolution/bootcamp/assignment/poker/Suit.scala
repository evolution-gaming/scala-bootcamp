package com.evolution.bootcamp.assignment.poker

sealed trait Suit
// TODO

object Suit {
  def of(x: Char): Either[ErrorMessage, Suit] = ???
}
