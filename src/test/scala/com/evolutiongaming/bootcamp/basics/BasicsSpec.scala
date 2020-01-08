package com.evolutiongaming.bootcamp.basics

import java.util.UUID

import Basics._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class BasicsSpec extends AnyFlatSpec {
  "hello" should "greet" in {
    val name = UUID.randomUUID().toString
    hello(name) shouldEqual s"Hello, $name!"
  }

  "add" should "add 2 and 3" in {
    add(2, 3) shouldEqual 5
  }
}
