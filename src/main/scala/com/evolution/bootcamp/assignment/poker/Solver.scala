package com.evolution.bootcamp.assignment.poker

import cats.syntax.either._

object Solver {
  def solve(testCase: TestCase): Either[ErrorMessage, Solution] = {
    // TODO: implement
    Solution(testCase.hands.map(x => Set(x))).asRight
  }

  def process(input: String): String = (for {
    testCase  <-  TestCase.of(input)
    solved    <-  solve(testCase)
  } yield solved.toString).leftMap(x => s"Error: $x").merge
}
