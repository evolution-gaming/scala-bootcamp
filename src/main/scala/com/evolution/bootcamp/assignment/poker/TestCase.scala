package com.evolution.bootcamp.assignment.poker

import cats.implicits._

final case class TestCase(
  board: Board,
  hands: List[Hand],
)

object TestCase {
  def of(x: String): Either[ErrorMessage, TestCase] = {
    val components = x.split("\\s+").toList
    components match {
      case b :: h if h.nonEmpty =>
        for {
          board <- Board.of(b)
          hands <- Hand.many(h)
        } yield TestCase(board, hands)

      case x => s"Failed to parse test case $x".asLeft
    }
  }
}
