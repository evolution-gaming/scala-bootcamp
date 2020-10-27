package com.evolution.bootcamp.assignment.poker

import cats.implicits._

sealed trait Value {
  def major: Int
  def descendingRanks: List[Rank]
}

object Value {
  implicit val ordering: Ordering[Value] = (x: Value, y: Value) => {
    val major = Ordering[Int].compare(x.major, y.major)
    if (major == 0) {
      0 // TODO: compare x.descendingRanks with y.descendingRanks
    } else major
  }

  def of(board: Board, hand: Hand): Value = {
    val options = hand match {
      case Hand.Texas(cards) =>
        (board.cards ++ cards).combinations(5)

      case Hand.Omaha(cards) =>
        for {
          b <- board.cards.combinations(3)
          h <- cards.combinations(2)
        } yield b ++ h
    }

    options.map(fiveCard).max
  }

  private def fiveCard(cards: List[Card]): Value = {
    val isStraight: Boolean = ???
    val isFlush: Boolean = ???

    val rankList: List[Int] = ???

    // AAAKK -> 3 :: 2 :: Nil
    // AAT87 -> 2 :: 1 :: 1 :: 1 :: Nil
    // AAAAK -> 4 :: 1 :: Nil

    val isFourOfAKind = rankList match {
      case 4 :: 1 :: Nil => true
      case _ => false
    }

    val attempts =
      StraightFlush.attempt(cards) ::
      // ...
      TwoPair.attempt(cards) ::
      Pair.attempt(cards) ::
      Nil

    attempts.collectFirstSome(identity).getOrElse(HighCard(cards))
  }

  final case class HighCard(set: Set[Rank]) extends Value {
    override def major: Int = 1
    override def descendingRanks: List[Rank] = ???
  }

  object HighCard {
    def apply(cards: List[Card]): HighCard = {
      HighCard(cards.map(_.rank).toSet)
    }
  }

  final case class Pair(pair: Rank, kickers: Set[Rank]) extends Value {
    override def major: Int = 2
    override def descendingRanks: List[Rank] = ???
  }

  object Pair {
    def attempt(cards: List[Card]): Option[Pair] = ???
  }

  final case class TwoPair(higher: Rank, lower: Rank, kicker: Rank) extends Value {
    override def major: Int = 3
    override def descendingRanks: List[Rank] = ???
  }

  object TwoPair {
    def attempt(cards: List[Card]): Option[TwoPair] = ???
  }

  final case class ThreeOfAKind(threeOfAKind: Rank, kickers: Set[Rank]) extends Value {
    override def major: Int = 4
    override def descendingRanks: List[Rank] = ???
  }

  final case class Straight(value: Rank) extends Value {
    override def major: Int = 5
    override def descendingRanks: List[Rank] = ???
  }

  final case class Flush(ranks: Set[Rank]) extends Value {
    override def major: Int = 6
    override def descendingRanks: List[Rank] = ???
  }

  final case class FullHouse(higher: Rank, lower: Rank) extends Value {
    override def major: Int = 7
    override def descendingRanks: List[Rank] = ???
  }

  final case class FourOfAKind(fourOfAKind: Rank, kicker: Rank) extends Value {
    override def major: Int = 8
    override def descendingRanks: List[Rank] = ???
  }

  final case class StraightFlush(value: Rank) extends Value {
    override def major: Int = 9
    override def descendingRanks: List[Rank] = ???
  }

  object StraightFlush {
    def attempt(cards: List[Card]): Option[StraightFlush] = ???
  }
}


