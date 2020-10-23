package com.evolutiongaming.bootcamp.assignment.poker

import scala.io.StdIn
import com.evolutiongaming.bootcamp.assignment.poker.Solver.process

object Main {
  def main(args: Array[String]): Unit = Iterator.continually(Option(StdIn.readLine()))
    .takeWhile(_.nonEmpty)
    .foreach { x =>
      x map process foreach println
    }
}
