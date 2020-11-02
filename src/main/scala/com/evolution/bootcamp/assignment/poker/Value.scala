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
    val isStraight: Boolean = {
      if ( cards.groupMapReduce(_.rank)(_ => 1)(_ + _).values.size < 5 ) false
      else {
        // https://www.baeldung.com/scala/sorting
        val valuesToCompare = cards.map(_.rank).sortWith(_.strength > _.strength)
        val result: Set[Int] = {for ( tuple <- valuesToCompare.sliding(2) ) yield tuple.head.strength - tuple.last.strength}.toSet

        result.subsetOf(Set(9, 1))
      }
    }

    val isFlush: Boolean = {
      if ( cards.groupMapReduce(_.suit)(_ => 1)(_ + _).values.size == 1 ) true
      else false
    }

    val rankList: List[Int] = {
      // https://stackoverflow.com/questions/11448685/scala-how-can-i-count-the-number-of-occurrences-in-a-list
      cards.groupMapReduce(_.rank)(_ => 1)(_ + _).values.toList.sorted(Ordering.Int.reverse)
    }

    val rankOrder: Seq[Rank] = {
      // https://alvinalexander.com/scala/how-to-sort-map-in-scala-key-value-sortby-sortwith/
      cards.groupBy(y => y.rank).toSeq.sortBy(x => x._2.size).reverse.map{ case (rank, _) => rank }
    }

    val isFourOfAKind = rankList match {
      case 4 :: 1 :: Nil => true
      case _ => false
    }

    val isFullHouse = rankList match {
      case 3 :: 2 :: Nil => true
      case _ => false
    }

    val isThreeOfAKind = rankList match {
      case 3 :: 1 :: 1 :: Nil => true
      case _ => false
    }

    val isTwoPair = rankList match {
      case 2 :: 2 :: 1 :: Nil => true
      case _ => false
    }

    val isPair = rankList match {
      case 2 :: 1 :: 1 :: 1 :: Nil => true
      case _ => false
    }

    if (isStraight && isFlush) StraightFlush.attempt(rankOrder)
    else if(isFourOfAKind)     FourOfAKind.attempt(rankOrder)
    else if(isFullHouse)       FullHouse.attempt(rankOrder)
    else if(isFlush)           Flush.attempt(rankOrder)
    else if(isStraight)        Straight.attempt(rankOrder)
    else if(isThreeOfAKind)    ThreeOfAKind.attempt(rankOrder)
    else if(isTwoPair)         TwoPair.attempt(rankOrder)
    else if(isPair)            Pair.attempt(rankOrder)
    else                       HighCard(rankOrder)
  }

  final case class HighCard(list: List[Rank]) extends Value {
    override def major: Int = 1
    override def descendingRanks: List[Rank] = list
  }

  object HighCard {
    def apply(rankOrder: Seq[Rank]): HighCard = HighCard( rankOrder.sortWith(_.strength > _.strength).toList )
  }

  //TODO: check descendingRanks: feels like there is something missing.
  final case class Pair(pair: Rank, kickers: List[Rank]) extends Value {
    override def major: Int = 2
    // https://alvinalexander.com/scala/how-add-elements-to-a-list-in-scala-listbuffer-immutable/
    override def descendingRanks: List[Rank] = pair :: kickers
  }

  object Pair {
    def attempt(rankOrder: Seq[Rank]): Pair = Pair(rankOrder.head, rankOrder.tail.sortWith(_.strength > _.strength).toList)
  }

  final case class TwoPair(higher: Rank, lower: Rank, kicker: Rank) extends Value {
    override def major: Int = 3
    override def descendingRanks: List[Rank] = List( higher, lower, kicker )
  }

  object TwoPair {
    def attempt(rankOrder: Seq[Rank]): TwoPair = {
      val pairs = List(rankOrder.head, rankOrder.tail.head).sortWith(_.strength > _.strength)

      TwoPair(pairs.head, pairs.last, rankOrder.tail.last)
    }
  }

  final case class ThreeOfAKind(threeOfAKind: Rank, kickers: List[Rank]) extends Value {
    override def major: Int = 4
    override def descendingRanks: List[Rank] = threeOfAKind :: kickers
  }

  object ThreeOfAKind {
    def attempt(rankOrder: Seq[Rank]): ThreeOfAKind = ThreeOfAKind(rankOrder.head, rankOrder.tail.sortWith(_.strength > _.strength).toList)
  }

  final case class Straight(value: Rank) extends Value {
    override def major: Int = 5
    override def descendingRanks: List[Rank] = List(value)
  }

  object Straight {
    def attempt(rankOrder: Seq[Rank]): Straight = Straight(rankOrder.sortWith(_.strength > _.strength).head)
  }

  final case class Flush(ranks: List[Rank]) extends Value {
    override def major: Int = 6
    override def descendingRanks: List[Rank] = ranks
  }

  object Flush {
    def attempt(rankOrder: Seq[Rank]): Flush = Flush(rankOrder.sortWith(_.strength > _.strength).toList)
  }

  final case class FullHouse(higher: Rank, lower: Rank) extends Value {
    override def major: Int = 7
    override def descendingRanks: List[Rank] = List(higher, lower)
  }

  object FullHouse {
    def attempt(rankOrder: Seq[Rank]): FullHouse = FullHouse(rankOrder.head, rankOrder.last)
  }

  final case class FourOfAKind(fourOfAKind: Rank, kicker: Rank) extends Value {
    override def major: Int = 8
    override def descendingRanks: List[Rank] = List(fourOfAKind, kicker)
  }

  object FourOfAKind {
    def attempt(rankOrder: Seq[Rank]): FourOfAKind = FourOfAKind(rankOrder.head, rankOrder.last)
  }

  final case class StraightFlush(value: Rank) extends Value {
    override def major: Int = 9
    override def descendingRanks: List[Rank] = List(value)
  }

  object StraightFlush {
    def attempt(rankOrder: Seq[Rank]): StraightFlush = {
      val checkFlushOrder: Seq[Rank] = rankOrder.sortWith(_.strength > _.strength)

      // Check if Ace is the lowest value
      if ( checkFlushOrder.head.toString == "A" && checkFlushOrder.tail.head.toString == "5" ) StraightFlush(checkFlushOrder.tail.head)
      else StraightFlush(checkFlushOrder.head)
    }
  }
}


