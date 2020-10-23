package com.evolutiongaming.bootcamp.cats.v2

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
class p8_MonadTransformersSpec extends AnyFlatSpec with Matchers {

  "Example" should "work fine" in {
    p8_MonadTransformers.httpMethod.unsafeRunSync() shouldBe "hello world"
  }
}
