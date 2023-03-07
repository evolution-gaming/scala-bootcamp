package com.evolutiongaming.bootcamp.cats.v4

import cats.syntax.applicative._
import cats.syntax.option._
import com.evolutiongaming.bootcamp.cats.v4.p4_Applicative.optionApplicative
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class p4_ApplicativeSpec extends AnyFlatSpec with Matchers {
  "EvoApplicative for Option" should "be implemented correctly" in {
    val pureOpt = optionApplicative.pure(40)
    pureOpt shouldBe 40.some
    pureOpt shouldBe 40.pure[Option]

    optionApplicative.ap[Int, String](Some(_.toString))(15.some) shouldBe Some("15")
  }
}
