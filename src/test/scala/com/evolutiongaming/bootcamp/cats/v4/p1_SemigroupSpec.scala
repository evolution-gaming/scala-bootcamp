package com.evolutiongaming.bootcamp.cats.v4

import cats.data.NonEmptyList
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

  "Nel semigroup" should "implement combine via concatenation" in {
    val combine = p1_Semigroup.semigroupNel.combine _

    combine(NonEmptyList.of(1, 2), NonEmptyList.of(3, 4)) shouldBe
      NonEmptyList.of(1, 2, 3, 4)

    combine(
      NonEmptyList.one(1),
      combine(NonEmptyList.one(2), NonEmptyList.one(3)),
    ) shouldBe
      combine(
        combine(NonEmptyList.one(1), NonEmptyList.one(2)),
        NonEmptyList.one(3),
      )
  }

  "Map semigroup" should "implement combine via concatenation" in {
    val combine = p1_Semigroup.semigroupMap.combine _

    combine(Map(1 -> 1, 2 -> 2), Map(3 -> 3, 4 -> 4)) shouldBe Map(1 -> 1, 2 -> 2, 3 -> 3, 4 -> 4)

    combine(Map(1 -> 1, 2 -> 1), Map(1 -> 2, 2 -> 2)) shouldBe Map(1 -> 2, 2 -> 2)
  }
}
