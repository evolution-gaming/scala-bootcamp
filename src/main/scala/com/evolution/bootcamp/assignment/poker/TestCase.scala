package com.evolution.bootcamp.assignment.poker

final case class TestCase(
  board: Board,
  hands: List[Hand],
)

object TestCase {
  def of(x: String): Either[ErrorMessage, TestCase] = ???
}
