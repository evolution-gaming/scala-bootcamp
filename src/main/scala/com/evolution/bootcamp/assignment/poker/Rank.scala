package com.evolution.bootcamp.assignment.poker

sealed trait Rank
// TODO

object Rank {
  def of(x: Char): Either[ErrorMessage, Rank] = ???
}
