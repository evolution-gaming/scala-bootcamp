package com.evolution.bootcamp.assignment.poker

sealed abstract class Rank private (val value: Char) {
  override def toString: String = value.toString
  def strength: Int = Rank.ordered.indexOf(this)
}

object Rank {
  case object Two extends Rank('2')
  case object Three extends Rank('3')
  case object Four extends Rank('4')
  case object Five extends Rank('5')
  case object Six extends Rank('6')
  case object Seven extends Rank('7')
  case object Eight extends Rank('8')
  case object Nine extends Rank('9')
  case object Ten extends Rank('T')
  case object Jack extends Rank('J')
  case object Queen extends Rank('Q')
  case object King extends Rank('K')
  case object Ace extends Rank('A')

  val ordered: List[Rank] =
    Two ::
    Three ::
    Four ::
    Five ::
    Six ::
    Seven ::
    Eight ::
    Nine ::
    Ten ::
    Jack ::
    Queen ::
    King ::
    Ace ::
    Nil

  def of(x: Char): Either[ErrorMessage, Rank] = {
    ordered.find(_.value == x).toRight(s"Unrecognized rank $x")
  }
}
