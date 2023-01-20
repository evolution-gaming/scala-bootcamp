package com.evolutiongaming.bootcamp.effects

import com.evolutiongaming.bootcamp.effects.EffectsHomework1.IO
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

class EffectsHomework1Spec extends AnyWordSpec with Matchers {
  import cats.effect.unsafe.implicits.global

  "IO" should {

    "have working `unsafeRunSync` (produce a pure value when run)" in {
      IO.pure(42).unsafeRunSync() shouldBe 42
    }

    "have working `map`" in {
      IO.pure(5).map(_ * 5).unsafeRunSync() shouldBe 25
      IO.pure(5).map(identity).unsafeRunSync() shouldBe 5
      IO.pure(5).map(_ => 100).unsafeRunSync() shouldBe 100
      IO.pure(42).map(_.toString).unsafeRunSync() shouldBe "42"
    }

    "have working `flatMap`" in {
      var i = 0
      (for {
        a  <- IO.pure(10)
        b  <- IO.pure(20)
        c  <- IO.pure(30)
        sum = a + b + c
        _  <- IO { i = sum }
      } yield sum).unsafeRunSync() shouldBe 60
      i shouldBe 60
    }

    "have working `*>`" in {
      (IO.pure(100) *> IO.pure(1)).unsafeRunSync() shouldBe 1
    }

    "have working `as`" in {
      IO.pure(100).as(1).unsafeRunSync() shouldBe 1
      IO.pure(100).as("result").unsafeRunSync() shouldBe "result"
    }

    "have working `void`" in {
      var i = 0
      IO { i = 10; i }.void.unsafeRunSync() shouldBe ()
      i shouldBe 10
    }

    "have working `attempt`" in {
      IO.pure(42).attempt.unsafeRunSync() shouldBe Right(42)
      val ex = new RuntimeException("error")
      IO(throw ex).attempt.unsafeRunSync() shouldBe Left(ex)
    }

    "have working `option`" in {
      IO.pure(42).option.unsafeRunSync() shouldBe Some(42)
      IO(throw new RuntimeException("error")).option.unsafeRunSync() shouldBe None
    }

    "have working `handleErrorWith`" in {
      IO.pure(42).handleErrorWith(_ => IO.pure(24)).unsafeRunSync() shouldBe 42
      IO(throw new RuntimeException("error")).handleErrorWith(_ => IO.pure(24)).unsafeRunSync() shouldBe 24
    }

    "have working `redeem`" in {
      IO.pure(42).redeem(_ => 24, identity).unsafeRunSync() shouldBe 42
      (IO.pure(42) *> IO(throw new RuntimeException("error"))).redeem(_ => 24, identity).unsafeRunSync() shouldBe 24
    }

    "have working `redeemWith`" in {
      IO.pure(42).redeemWith(_ => IO.pure(24), IO.pure).unsafeRunSync() shouldBe 42
      (IO.pure(42) *> IO[Int](throw new RuntimeException("error")))
        .redeemWith(_ => IO.pure(24), IO.pure)
        .unsafeRunSync() shouldBe 24
    }

    "have working `unsafeToFuture`" in {
      import concurrent.ExecutionContext.Implicits.global
      Await.result(IO.pure(42).unsafeToFuture(), Duration.Inf) shouldBe 42
    }
  }

  "IO companion object" should {

    "have working `apply`" in {
      var i = 0
      IO { i = 42; i }.unsafeRunSync() shouldBe 42
      i shouldBe 42
    }

    "have working `suspend`" in {
      IO.suspend(IO.pure(42)).unsafeRunSync() shouldBe 42
    }

    "have working `delay`" in {
      var i = 0
      IO.delay { i = 42; i }.unsafeRunSync() shouldBe 42
      i shouldBe 42
    }

    "have working `pure`" in {
      IO.pure(42).unsafeRunSync() shouldBe 42
    }

    "have working `fromEither`" in {
      IO.fromEither(Left(new RuntimeException("error"))).handleErrorWith(_ => IO.pure(24)).unsafeRunSync() shouldBe 24
      IO.fromEither(Right(42)).unsafeRunSync() shouldBe 42
    }

    "have working `fromOption`" in {
      IO.fromOption(None)(new RuntimeException("error")).handleErrorWith(_ => IO.pure(24)).unsafeRunSync() shouldBe 24
      IO.fromOption(Some(42))(new RuntimeException("error")).unsafeRunSync() shouldBe 42
    }

    "have working `fromTry`" in {
      IO.fromTry(Try(throw new RuntimeException("error"))).handleErrorWith(_ => IO.pure(24)).unsafeRunSync() shouldBe 24
      IO.fromTry(Try(42)).unsafeRunSync() shouldBe 42
    }

    "have working `none`" in {
      IO.none.unsafeRunSync() shouldBe None
    }

    "have working `raiseError`" in {
      IO.raiseError(new RuntimeException("error")).handleErrorWith(_ => IO.pure(24)).unsafeRunSync() shouldBe 24
    }

    "have working `raiseUnless`" in {
      IO.raiseUnless(cond = true)(new RuntimeException("error")).as(42).unsafeRunSync() shouldBe 42
      IO.raiseUnless(cond = false)(new RuntimeException("error"))
        .as(42)
        .handleErrorWith(_ => IO.pure(24))
        .unsafeRunSync() shouldBe 24
    }

    "have working `raiseWhen`" in {
      IO.raiseWhen(cond = false)(new RuntimeException("error")).as(42).unsafeRunSync() shouldBe 42
      IO.raiseWhen(cond = true)(new RuntimeException("error"))
        .as(42)
        .handleErrorWith(_ => IO.pure(24))
        .unsafeRunSync() shouldBe 24
    }

    "have working `unlessA`" in {
      var i = 0
      IO.unlessA(cond = true)(IO { i = 42 }).unsafeRunSync() shouldBe ()
      i shouldBe 0
      IO.unlessA(cond = false)(IO { i = 42 }).unsafeRunSync() shouldBe ()
      i shouldBe 42
    }

    "have working `whenA`" in {
      var i = 0
      IO.whenA(cond = false)(IO { i = 42 }).unsafeRunSync() shouldBe ()
      i shouldBe 0
      IO.whenA(cond = true)(IO { i = 42 }).unsafeRunSync() shouldBe ()
      i shouldBe 42
    }

    "have working `unit`" in {
      IO.unit.unsafeRunSync() shouldBe ()
    }
  }
}
