package com.evolutiongaming.bootcamp.cats.v2

import cats.syntax.semigroup._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class p2_MonoidSpec extends AnyFlatSpec with Matchers {
  "Int monoid" should "be correctly implemented" in {
    implicit val intMonoid = p2_Monoid.intMonoid
    val empty = p2_Monoid.intMonoid.empty

    3 |+| 3 shouldBe 9
    2 |+| (3 |+| 3) shouldBe (2 |+| 3) |+| 3
    empty |+| 9 shouldBe 9
  }

  "String monoid" should "be correctly implemented" in {
    implicit val stringMonoid = p2_Monoid.stringMonoid
    val empty = p2_Monoid.stringMonoid.empty

    "hello " |+| "world" shouldBe "hello world"
    "a" |+| ("b" |+| "c") shouldBe ("a" |+| "b") |+| "c"
    empty |+| "hello" shouldBe "hello"
  }

  "Boolean monoid" should "be correctly implemented" in {
    implicit val boolMonoid = p2_Monoid.boolMonoid
    val empty = p2_Monoid.boolMonoid.empty

    true |+| true shouldBe true
    true |+| false shouldBe false
    (true |+| false) |+| true shouldBe true |+| (false |+| true)

    empty |+| true shouldBe true
  }
}
