package com.evolution.bootcamp.assignment.poker

import cats.syntax.either._

object Solver {
  def sortConsideringEquals[K, V : Ordering](input: List[(K, V)]): List[Set[K]] = {
    input
      .groupBy { case (_, v) => v }
      .toList
      .sortBy { case (v, _) => v }
      .map { case (_, listOfK) =>
        listOfK
          .map { case (k, _) => k }
          .toSet
      }
  }

  def solve(testCase: TestCase): Either[ErrorMessage, Solution] = {
    val pairs = testCase.hands.map { hand =>
      hand -> Value.of(testCase.board, hand)
    }

    import Value._
    val sorted = sortConsideringEquals(pairs)

    Solution(sorted).asRight
  }

  def process(input: String): String = (for {
    testCase  <-  TestCase.of(input)
    solved    <-  solve(testCase)
  } yield solved.toString).leftMap(x => s"Error: $x").merge
}
