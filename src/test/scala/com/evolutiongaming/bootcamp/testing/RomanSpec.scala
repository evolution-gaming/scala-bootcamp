package com.evolutiongaming.bootcamp.testing

import org.scalacheck.Gen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalacheck.Test.Parameters

class RomanSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks with Matchers {
  implicit val params = Parameters.default.withMinSuccessfulTests(1000)

  "decimal" should "invert roman" in {
    forAll(Gen.choose(1, 1000000)) { _ =>

    }
  }
}
