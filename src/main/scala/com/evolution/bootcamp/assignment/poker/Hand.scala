package com.evolution.bootcamp.assignment.poker

sealed trait Hand
object Hand {
  final case class Texas(cards: List[Card]) extends Hand
  final case class Omaha(cards: List[Card]) extends Hand
}
