package com.evolutiongaming.bootcamp.basics

import ControlStructures._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalacheck.Gen._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class ControlStructuresSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {
  "applyNTimesForInts" should "work correctly" in {
    forAll(choose(1, 1000)) { a: Int =>
      forAll(choose(1, 1000)) { b: Int =>
        applyNTimesForInts(_ + 1, a)(b) shouldEqual a + b
      }
    }
  }

  "applyNTimes" should "work correctly" in {
    forAll(choose(1, 1000)) { n: Int =>
      forAll(choose(1, 10000)) { a: Int =>
        forAll(choose(1, 500)) { b: Int =>
          applyNTimes[Int](_ + b, n)(a) shouldEqual a + n * b
        }
      }
    }
  }
}
