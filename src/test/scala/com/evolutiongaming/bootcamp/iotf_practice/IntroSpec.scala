package com.evolutiongaming.bootcamp.iotf_practice

import cats.Id
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class IntroSpec extends AnyFlatSpec with Matchers {
  "logAndSend-1" should "return true from response" in {
    import Intro.`1`._
    import cats.effect.unsafe.implicits.global
    import cats.effect.IO

    val log: Log       = _ => IO.unit
    val sendTrue: Send = _ => IO.pure(Response(200, true))

    logAndSend(42, log, sendTrue).unsafeRunSync() shouldBe true
  }

  "logAndSend-2" should "return true from response" in {
    import Intro.`2`._
    /*
     * Task: implement the same test, but using `cats.Id` instead of `cats.IO`
     */
    // logAndSend[Id](42) shouldBe true
  }
}
