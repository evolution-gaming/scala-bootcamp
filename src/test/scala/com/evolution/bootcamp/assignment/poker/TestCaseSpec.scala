package com.evolution.bootcamp.assignment.poker

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.syntax.either._

class TestCaseSpec extends AnyFlatSpec with Matchers {
  it should "parse" in {
    val serialized = "4cKs4h8s7s Ad4s Ac4d As9s KhKd 5d6d"
    val board = Board(List(
      Card(Rank.Four, Clubs),
      Card(Rank.King, Spades),
      Card(Rank.Four, Hearts),
      Card(Rank.Eight, Spades),
      Card(Rank.Seven, Spades),
    ))
    val hands = List(
      Hand.Texas(List(Card(Rank.Ace, Diamonds), Card(Rank.Four, Spades))),
      Hand.Texas(List(Card(Rank.Ace, Clubs), Card(Rank.Four, Diamonds))),
      Hand.Texas(List(Card(Rank.Ace, Spades), Card(Rank.Nine, Spades))),
      Hand.Texas(List(Card(Rank.King, Hearts), Card(Rank.King, Diamonds))),
      Hand.Texas(List(Card(Rank.Five, Diamonds), Card(Rank.Six, Diamonds))),
    )
    TestCase.of(serialized) shouldEqual TestCase(board, hands).asRight
  }
}
