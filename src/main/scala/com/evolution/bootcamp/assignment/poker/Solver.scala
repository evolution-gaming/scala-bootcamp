package com.evolution.bootcamp.assignment.poker

import cats.syntax.either._

object Solver {
  def solve(testCase: TestCase): Either[ErrorMessage, Solution] = ???

  def process(input: String): String = (for {
    testCase  <-  TestCase.of(input)
    solved    <-  solve(testCase)
  } yield solved.toString).leftMap(x => s"Error: $x").merge
}
