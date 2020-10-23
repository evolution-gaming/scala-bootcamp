package com.evolutiongaming.bootcamp.cats.v2

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class p3_FunctorSpec extends AnyFlatSpec with Matchers {
  "Functor for List" should "be implemented correctly" in {
    p3_Functor.listFunctor.map(List(1, 2, 3))(_.toString) shouldBe List("1", "2", "3")
  }

  "Functor for Option" should "be implemented correctly" in {
    p3_Functor.optFunctor.map(Some(3))(_ * 2) shouldBe Some(6)
    p3_Functor.optFunctor.map(Option.empty[Int])(_ * 2) shouldBe None
  }
}
