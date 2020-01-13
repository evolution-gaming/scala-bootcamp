package com.evolutiongaming.bootcamp.basics

object ControlStructures {
  // You can follow your progress using the tests in `ControlStructuresSpec`.

  // TODO: Control Structures: https://docs.scala-lang.org/style/control-structures.html
  // TODO: Recursion - and alternative (foldLeft, reduce)
  // TODO: if-then-else
  // TODO: Pattern matching - case-match
  // TODO: map, flatMap, for comprehensions
  // TODO: exceptions, try-finally & Try

  // Exercise: Implement a function `applyNTimes` which takes a function `f` and an integer `n` and
  // returns a function which applies the function `f` `n` times.
  //
  // Thus `applyNTimesForInts(_ + 1, 4)(3)` should return `((((3 + 1) + 1) + 1) + 1)` or `7`.
  def applyNTimesForInts(f: Int => Int, n: Int): Int => Int = { x: Int =>
    f(x + n) // replace with a correct implementation
  }

  // Exercise: Convert the function `applyNTimesForInts` into a polymorphic function `applyNTimes`:
  def applyNTimes[A](f: A => A, n: Int): A => A = { x: A =>
    // replace with correct implementation
    println(n)
    f(x)
  }

  // Exercise:
  //
  // Given:
  //  A = Set(0, 1, 2)
  //  B = Set(true, false)
  //
  // List all the elements in `A * B`.
  //
  val AProductB: Set[(Int, Boolean)] = Set()

  // Exercise:
  //
  // Given:
  // A = { 0, 1, 2 }
  // B = { true, false }
  //
  // List all the elements in `A + B`.
  //
  val ASumB: Set[Either[Int, Boolean]] = Set()
}
