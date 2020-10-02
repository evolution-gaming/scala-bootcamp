package com.evolutiongaming.bootcamp.basics

import DataStructures._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class DataStructuresSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {
  "allSubSetsOfSizeN" should "work correctly on 2 from Set(1, 2, 3)" in {
    allSubsetsOfSizeN(Set(1, 2, 3), 2) shouldEqual Set(Set(1, 2), Set(2, 3), Set(1, 3))
  }

  it should "work correctly" in {
    def fact(num: Int): BigDecimal = {
      (1 to num).map(x => BigDecimal.valueOf(x.toLong)).foldLeft(BigDecimal.valueOf(1)) ((a, b) => a * b)
    }

    val set = (0 until 16).toSet
    (1 to 4) foreach { k =>
      val obtained = allSubsetsOfSizeN(set, k)
      val n = set.size
      val expectedSize = fact(n) / (fact(k) * fact(n - k))
      obtained.size shouldEqual expectedSize.toLong
      obtained.forall(_.size == k) shouldEqual true
    }
  }

  "totalVegetableCost" should "be correct" in {
    totalVegetableCost shouldEqual 5012
  }

  "totalVegetableWeights" should "be correct" in {
    totalVegetableWeights shouldEqual Map(
      "cucumbers" -> 6460,
      "olives" -> 64,
    )
  }

  "allEqual" should "work for lists which are all equal" in {
    allEqual(List("a", "a", "a", "a")) shouldBe true
  }

  "allEqual" should "work on 1 element list" in {
    allEqual(List("a")) shouldBe true
  }

  "allEqual" should "work for lists which are NOT all equal" in {
    allEqual(List("a", "a", "b", "a")) shouldBe false
  }

  "sort considering equal values" should "be correct on example 1" in {
    val input = Map("a" -> 1, "b" -> 2, "c" -> 4, "d" -> 1, "e" -> 0, "f" -> 2, "g" -> 2)
    val expected = List(Set("e") -> 0, Set("a", "d") -> 1, Set("b", "f", "g") -> 2, Set("c") -> 4)
    val obtained = sortConsideringEqualValues(input)
    obtained shouldEqual expected
  }

   it should "be correct on example 2" in {
    val values = Set("a1", "a2", "b1", "c1", "c2", "d1").map { x =>
      x -> x.head.toInt
    }.toMap

    sortConsideringEqualValues(values) shouldEqual List(
      Set("a1", "a2") -> 'a'.toInt,
      Set("b1") -> 'b'.toInt,
      Set("c1", "c2") -> 'c'.toInt,
      Set("d1") -> 'd'.toInt,
    )
  }
}
