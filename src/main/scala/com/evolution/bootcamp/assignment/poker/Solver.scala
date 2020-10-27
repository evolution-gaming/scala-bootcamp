package com.evolution.bootcamp.assignment.poker

import cats.syntax.either._

object Solver {
  type ErrorMessage = String

  def parseTestCase(input: String): Either[ErrorMessage, TestCase] = ???

  def solve(testCase: TestCase): Either[ErrorMessage, Solution] = ???

  def printSolution(solution: Solution): String = ???

  def process(input: String): String = (for {
    testCase  <-  parseTestCase(input)
    solved    <-  solve(testCase)
    result    =   printSolution(solved)
  } yield result).leftMap(x => s"Error: $x").merge
}
