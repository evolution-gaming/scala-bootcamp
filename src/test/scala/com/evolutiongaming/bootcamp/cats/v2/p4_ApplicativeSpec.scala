package com.evolutiongaming.bootcamp.cats.v2

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
class p4_ApplicativeSpec extends AnyFlatSpec with Matchers {
  "EvoApplicative for Option" should "be implemented correctly" in {
    p4_Applicative.optionApplicative.pure(40) shouldBe Some(40)
  }

}
