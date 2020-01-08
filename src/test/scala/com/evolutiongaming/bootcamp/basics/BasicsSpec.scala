package com.evolutiongaming.bootcamp.basics

import Basics._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalacheck.Arbitrary._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class BasicsSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {
  "hello" should "work for all strings" in {
    forAll { x: String =>
      hello(x) shouldEqual s"Hello, $x!"
    }
  }

  "add" should "add 2 and 3" in {
    add(2, 3) shouldEqual 5
  }

  it should "work for all numbers" in {
    forAll { (a: Int, b: Int) =>
      add(a, b) shouldEqual a + b
    }
  }

  "AllBooleans" should "contain all possible boolean values" in {
    AllBooleans.size shouldEqual 2
    AllBooleans.reduce(_ && _) shouldEqual false
    AllBooleans.reduce(_ || _) shouldEqual true
  }
}
