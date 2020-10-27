package com.evolution.bootcamp.assignment.poker

import com.evolution.bootcamp.assignment.poker.Solver.process

import scala.io.Source

object Main extends App {
  Source.stdin.getLines() map process foreach println
}
