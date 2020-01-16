package com.evolutiongaming.bootcamp.basics

import ControlStructures._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalacheck.Gen._
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class ControlStructuresSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {
  private def checkFizzBuzz(f: Int => String): Assertion = {
    val obtained = (0 to 100).toList map f
    // we're doing it this way here to avoid providing the answer to the exercise by implementing the function
    val expected = List("fizzbuzz", "1", "2", "fizz", "4", "buzz", "fizz", "7", "8", "fizz", "buzz", "11", "fizz", "13", "14", "fizzbuzz", "16", "17", "fizz", "19", "buzz", "fizz", "22", "23", "fizz", "buzz", "26", "fizz", "28", "29", "fizzbuzz", "31", "32", "fizz", "34", "buzz", "fizz", "37", "38", "fizz", "buzz", "41", "fizz", "43", "44", "fizzbuzz", "46", "47", "fizz", "49", "buzz", "fizz", "52", "53", "fizz", "buzz", "56", "fizz", "58", "59", "fizzbuzz", "61", "62", "fizz", "64", "buzz", "fizz", "67", "68", "fizz", "buzz", "71", "fizz", "73", "74", "fizzbuzz", "76", "77", "fizz", "79", "buzz", "fizz", "82", "83", "fizz", "buzz", "86", "fizz", "88", "89", "fizzbuzz", "91", "92", "fizz", "94", "buzz", "fizz", "97", "98", "fizz", "buzz")
    obtained shouldEqual expected
  }

  "fizzBuzz1" should "work correctly" in {
    checkFizzBuzz(fizzBuzz1)
  }

  "fizzBuzz2" should "work correctly" in {
    checkFizzBuzz(fizzBuzz2)
  }

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

  "makeTransfer" should "work correctly" in {
    makeTransfer("valid.200", "valid.25", 50) shouldEqual Right((150, 75))
    makeTransfer("valid.10", "valid.20", 7) shouldEqual Right((3, 27))
    makeTransfer("invalid", "valid.200", 50) shouldBe a[Left[_, _]]
  }
}
