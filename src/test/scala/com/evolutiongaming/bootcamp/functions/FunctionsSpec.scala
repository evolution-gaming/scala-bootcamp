package com.evolutiongaming.bootcamp.functions

import com.evolutiongaming.bootcamp.functions.Functions._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class FunctionsSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {

  "isEven" should "work correctly" in {
    forAll { n: Int =>
      val r = n % 2 == 0
      isEven(n) shouldBe r
      isEvenFunc(n) shouldBe r
      isEvenMethodToFunc(n) shouldBe r
    }
  }

  "mapOption" should "work correctly" in {
    forAll { n: Int =>
      mapOption[Int, String](Some(n), _.toString + "!") should contain(s"$n!")
    }
  }
}
