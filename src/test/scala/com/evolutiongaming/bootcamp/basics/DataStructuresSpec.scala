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
}
