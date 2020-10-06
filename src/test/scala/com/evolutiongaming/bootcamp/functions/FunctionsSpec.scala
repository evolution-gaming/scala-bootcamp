package com.evolutiongaming.bootcamp.functions

import Functions._
import FunctionsDescoped._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class FunctionsSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {

  "isEven" should "work correctly" in {
    isEven(2) shouldEqual true
    isEven(3) shouldEqual false

    isEven(-2) shouldEqual true
    isEven(-3) shouldEqual false
  }

  "identity" should "work correctly" in {
    identity(3) shouldEqual 3
    identity("foo") shouldEqual "foo"
  }

  // --

  "asString function" should "print a proper json" in {
    asString(data).replaceAll("\\s", "") shouldEqual
      "{\"username\":\"John\",\"address\":{\"country\":\"UK\",\"postalCode\":45765},\"eBooks\":[\"Scala\",\"Dotty\"]}"
  }

  "isContainsNegative function" should "return that the data does not contains negative numbers" in {
    isContainsNegative(data) shouldEqual false
  }

  "nestingLevel function" should "return the correct nesting level" in {
    nestingLevel(data) shouldEqual 2
  }

}
