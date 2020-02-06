package com.evolutiongaming.bootcamp.testing

import org.scalacheck.Gen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalacheck.Test.Parameters


class JsonSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks with Matchers {
  import Json._

  implicit val params = Parameters.default.withMinSuccessfulTests(1000)

  def jsonGen: Gen[Json] =
    ???

  "parse" should "invert print" in {
    forAll(jsonGen) { _ =>

    }
  }
}
