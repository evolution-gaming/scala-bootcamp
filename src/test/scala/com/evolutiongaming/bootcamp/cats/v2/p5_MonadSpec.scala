package com.evolutiongaming.bootcamp.cats.v2

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class p5_MonadSpec extends AnyFlatSpec with Matchers {
 "EvoMonad for List" should "be implemented correctly" in {
   val m = p5_Monad.listM

   m.pure(1) shouldBe List(1)
   m.flatMap(List(1, 2))(x => List(x, x)) shouldBe List(1,1,2,2)
   m.map(List(1, 2, 3))(_ * 2) shouldBe List(2, 4, 6)
 }

  "EvoMonad for Option" should "be implemented correctly" in {
    val m = p5_Monad.optionM

    m.pure(1) shouldBe Some(1)
    m.flatMap(Some(3))(x => Some(x.toString)) shouldBe Some("3")
    m.map(Some("Hey there"))(_.toUpperCase) shouldBe Some("HEY THERE")
  }
}
