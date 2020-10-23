package com.evolutiongaming.bootcamp.cats.v2

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class p1_SemigroupSpec extends AnyFlatSpec with Matchers {

  "String semigroup" should "implement combine via string concatenation" in {
    val combine = p1_Semigroup.stringSemigroup.combine _

    combine("hello ", "world") shouldBe "hello world"
    combine("hello ", combine("world", "!")) shouldBe combine(combine("hello ", "world"), "!")
  }

  "Int semigroup" should "implement combine with an sum as an operation" in {
    val combine = p1_Semigroup.intSemigroup.combine _

    combine(1, 3) shouldBe 4
    combine(1, combine(2, 3)) shouldBe combine(combine(1, 2), 3)
  }

  "List semigroup" should "implement combine via list concatenation" in {
    val combine = p1_Semigroup.listSemigroup[Int].combine _

    combine(List(1, 2), List(3, 4)) shouldBe List(1, 2, 3, 4)
    combine(List(1), combine(List(2), List(3))) shouldBe combine(combine(List(1), List(2)), List(3))
  }
}
