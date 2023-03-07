package com.evolutiongaming.bootcamp.cats.v4

import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class p3_FunctorSpec extends AsyncWordSpec with AsyncIOSpec with Matchers {
  import p3_Functor._

  "Functor for Option" should {
    "be implemented correctly" in {
      optFunctor.map(Some(3))(_ * 2) shouldBe Some(6)
      optFunctor.map(Option.empty[Int])(_ * 2) shouldBe None
    }
  }

  "Functor for List" should {
    "be implemented correctly" in {
      listFunctor.map(List(1, 2, 3))(_.toString) shouldBe List("1", "2", "3")
    }
  }
}
