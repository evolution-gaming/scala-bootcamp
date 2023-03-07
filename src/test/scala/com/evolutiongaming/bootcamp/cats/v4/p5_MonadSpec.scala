package com.evolutiongaming.bootcamp.cats.v4

import com.evolutiongaming.bootcamp.cats.v4.p5_Monad.{listMonad, optionMonad}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class p5_MonadSpec extends AnyFlatSpec with Matchers {
  "Monad for List" should "be implemented correctly" in {
    listMonad.pure(1) shouldBe List(1)
    listMonad.flatMap(List(1, 2, 3))(x => List.fill(x)(x)) shouldBe List(1, 2, 2, 3, 3, 3)
    listMonad.map(List(1, 2, 3))(_ * 2) shouldBe List(2, 4, 6)
  }

  "Monad for Option" should "be implemented correctly" in {
    optionMonad.pure(1) shouldBe Some(1)
    optionMonad.flatMap(Some(3))(x => Some(x.toString)) shouldBe Some("3")
    optionMonad.map(Some("Hey there"))(_.toUpperCase) shouldBe Some("HEY THERE")
  }
}
