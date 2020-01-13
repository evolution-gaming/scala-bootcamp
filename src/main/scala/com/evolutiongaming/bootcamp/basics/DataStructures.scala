package com.evolutiongaming.bootcamp.basics

object DataStructures {
  // TODO: Mutable vs Immutable collections
  // TODO: Most commonly used collections - Array-s, List-s, Vector-s, Set-s, Map-s, Seq-s, etc.

  // You can follow your progress using the tests in `DataStructuresSpec`.

  // Ranges and Sequences
  val inclusiveRange: Seq[Int] = 2 to 4
  val exclusiveRange: Seq[Int] = 2 until 4

  // Exercise: Return a set with all subsets of the provided set `set` with `n` elements
  // For example, `allSubsetsOfSizeN(Set(1, 2, 3), 2) == Set(Set(1, 2), Set(2, 3), Set(1, 3))`.
  def allSubsetsOfSizeN[A](set: Set[A], n: Int): Set[Set[A]] = {
    // replace with correct implementation
    println(n)
    Set(set)
  }
}
