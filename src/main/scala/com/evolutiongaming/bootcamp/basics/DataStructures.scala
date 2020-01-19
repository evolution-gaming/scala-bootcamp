package com.evolutiongaming.bootcamp.basics

import scala.util.Try

object DataStructures {
  // You can follow your progress using the tests in `DataStructuresSpec`.

  // Mutable vs Immutable collections

  // Both mutable and immutable collections are provided by the Scala standard library
  // Mutable collections can be updated or extended in place and doing so is a side effect.

  // Immutable collections never change, however they have operations that return new collections with
  // added, removed or updated elements. The original collection, however, remains unchanged.

  val mutableList = scala.collection.mutable.ListBuffer(1, 2, 3)
  mutableList.update(1, -1)

  val immutableList1 = scala.collection.immutable.List(1, 2, 3)
  val updatedImmutableList = immutableList1.updated(1, -1)

  val doTheyHaveEqualContents = (mutableList zip updatedImmutableList) forall { case (a, b) => a == b }

  // Arrays
  //
  // Arrays are mutable, indexed collections which are equivalent to Java's array's. They are indexed from 0.
  val array = Array(1, 2, 3, 4, 5)
  array(2) // read the 3rd element, it will be 3
  array(3) = 7 // update the 4th element to be 7

  // Note that we can't reassign `val array` to a different array.

  // Lists
  //
  // Immutable list represents ordered collections of elements of type A.
  // It is optimal for last-in-first-out (LIFO) or stack like access patterns. Prepending an element
  // or obtaining the "tail" (all elements except the first one) are fast operations.

  val immutableList2 = List(1, 2, 3)
  val immutableList3 = 1 :: 2 :: 3 :: Nil
  require(immutableList2 == immutableList3) // the two ways above are the same

  val emptyList1 = Nil
  val emptyList2 = List()
  val emptyList3 = List.empty

  val prepend4 = 4 :: immutableList2 // 4 :: 1 :: 2 :: 3 :: Nil
  val prepend42 = 5 :: immutableList2 // 5 :: 1 :: 2 :: 3 :: Nil
  val tailOfList = immutableList2.tail // 2 :: 3 :: Nil

  val joinLists = immutableList2 ::: List(8, 9) // 1 :: 2 :: 3 :: 8 :: 9 :: Nil

  val headOfList1 = Try(emptyList1.head)// what will happen here?!
  val headOfList2 = emptyList1.headOption // None
  val headOfList3 = immutableList2.headOption // Some(1)

  // Question. We have seen `Nil`, `None`, `Nothing` and `null` so far. What do they each mean?

  // Vectors
  //
  // Vectors are general purpose, immutable data structures with effectively constant time random access
  // and updates, as well as fast append and prepend.

  val vector1 = Vector(1, 2, 3, 4)
  val vector2 = 5 +: vector1 // prepend
  val vector3 = vector1 :+ 6 // append
  val vector4 = vector2 ++ vector3 // concatenate

  // Sets
  //
  // Sets are data structures that don't contain any duplicate elements.

  val vegetables = Set("tomatoes", "peppers", "pumpkins", "cucumbers", "olives")
  vegetables("tomatoes") // true
  vegetables("apple") // false
  vegetables.contains("tomatoes") // true, same thing

  val moreVegetables = vegetables + "avocado"
  val lessVegetables = moreVegetables - "peppers"

  // Maps
  //
  // Maps consist of pairs of keys and values and usually offer fast lookup by key.

  val vegetableWeights = Map(
    ("pumpkins", 10),
    ("cucumbers", 20),
    ("olives", 2),
  )

  val vegetablePrices = Map(
    "tomatoes" -> 4,
    "peppers" -> 5,
    "olives" -> 17,
  )

  val moreVegetablePrices = vegetablePrices + ("pumpkins" -> 3)
  val lessVegetableWeights = vegetableWeights - "pumpkins"

  val questionableMap = vegetableWeights ++ vegetablePrices

  // Question. Why should `questionableMap` be considered questionable?

  val vegetableAmounts = Map(
    "tomatoes" -> 17,
    "peppers" -> 234,
    "olives" -> 32,
    "cucumbers" -> 323,
  )

  // Exercise. Calculate the total cost of all vegetables, taking vegetable amounts (in units) from
  // `vegetableAmounts` and prices per unit from `vegetablePrices`. Assume the price is 10 if not available
  // in `vegetablePrices`.
  val totalVegetableCost: Int = {
    17 // implement here
  }

  // Exercise. Given the vegetable weights (per 1 unit of vegetable) in `vegetableWeights` and vegetable
  // amounts (in units) in `vegetableAmounts`, calculate the total weight per type of vegetable, if known.
  //
  // For example, the total weight of "olives" is 2 * 32 == 64.
  val totalVegetableWeights: Map[String, Int] = { // implement here
    Map()
  }

  // Ranges and Sequences
  val inclusiveRange: Seq[Int] = 2 to 4
  val exclusiveRange: Seq[Int] = 2 until 4

  // Seq, IndexedSeq and LinearSeq traits are implemented by many collections and contain various useful
  // methods. See https://docs.scala-lang.org/overviews/collections/seqs.html in case you are interested
  // to learn more about them at this point.

  // The collections API is rich and implements a large number of useful methods, such as:
  // - contains
  // - containsSlice
  // - count
  // - distinct
  // - drop
  // - dropWhile
  // - empty
  // - endsWith
  // - exists
  // - filter
  // - filterNot
  // - find
  // - flatMap
  // - flatten
  // - foldLeft
  // - foldRight
  // - forall
  // - head
  // - headOption
  // - init
  // - intersect
  // - last
  // - lastOption
  // - map
  // - max
  // - min
  // - nonEmpty
  // - partition
  // - reverse
  // - size
  // - slice
  // - sort
  // - sortWith
  // - startsWith
  // - tail
  // - take
  // - takeRight
  // - takeWhile
  // - zip

  // Exercise: Return a set with all subsets of the provided set `set` with `n` elements
  // For example, `allSubsetsOfSizeN(Set(1, 2, 3), 2) == Set(Set(1, 2), Set(2, 3), Set(1, 3))`.
  def allSubsetsOfSizeN[A](set: Set[A], n: Int): Set[Set[A]] = {
    // replace with correct implementation
    println(n)
    Set(set)
  }
}
