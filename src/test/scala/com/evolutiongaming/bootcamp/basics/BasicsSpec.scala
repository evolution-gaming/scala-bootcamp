package com.evolutiongaming.bootcamp.basics

import java.util.UUID
import Basics._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalacheck.Arbitrary._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class BasicsSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {
  "hello" should "greet random UUID" in {
    val name = UUID.randomUUID().toString
    hello(name) shouldEqual s"Hello, $name!"
  }

  it should "work for all strings" in {
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
}
