package com.evolutiongaming.bootcamp.assignment.poker

object Solver {
  // TODO: implement solution logic
  def process(line: String): String = {
    val ErrorPrefix = "Error: "

    line.toLowerCase.split("\\s+").toList match {
      case "texas-holdem" :: board :: hands   => hands.sorted.mkString(" ")
      case "omaha-holdem" :: board :: hands   => ErrorPrefix + "The solution doesn't support Omaha Hold'em"
      case "five-card-draw" :: hands          => ErrorPrefix + "The solution doesn't support Five Card Draw"
      case x :: _                             => ErrorPrefix + "Unrecognized game type"
      case _                                  => ErrorPrefix + "Invalid input"
    }
  }
}
