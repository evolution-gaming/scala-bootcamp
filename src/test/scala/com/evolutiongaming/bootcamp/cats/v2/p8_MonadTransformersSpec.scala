package com.evolutiongaming.bootcamp.cats.v2

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
class p8_MonadTransformersSpec extends AnyFlatSpec with Matchers {
  import cats.effect.unsafe.implicits.global

  "Example" should "work fine" in {
    p8_MonadTransformers.httpMethod.unsafeRunSync() shouldBe "hello world"
  }
}
