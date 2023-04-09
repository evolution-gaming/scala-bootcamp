package com.evolutiongaming.bootcamp.cats.v4

import cats.ApplicativeError
import cats.effect.IO
import cats.syntax.either._
import com.evolutiongaming.bootcamp.cats.v4.p6_MonadErrorSpec._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class p6_MonadErrorSpec extends AnyFlatSpec with Matchers {

  /** Consider a naive Retrier that can run with Either, Option, IO as an effect and makes `n` attempts to complete an effectful operation f.
    * After it hits maxAttempts` it gives up and raises an error to the callee.
    */
  object Retrier {
    private def _retry[F[_], E, A](
      f: Int => F[A],
      numAttempts: Int,
      maxAttempts: Int,
    )(implicit AE: ApplicativeError[F, E]): F[A] =
      // implement using handleErrorWith and raiseError
      // import cats.syntax.applicativeError._
      ???

    def retry[F[_], E, A](f: Int => F[A], maxAttempts: Int)(implicit AE: ApplicativeError[F, E]): F[A] =
      _retry(f, 0, maxAttempts)
  }

  "MonadError retrier" should "works with Either" in {
    def succeedsAfterNCallsEither(succeedAfter: Int)(n: Int): Either[String, String] =
      if (succeedAfter == n) Right("Good") else Left("Bad")

    Retrier.retry(succeedsAfterNCallsEither(3), 1) shouldBe Left("Bad")
    Retrier.retry(succeedsAfterNCallsEither(3), 2) shouldBe Left("Bad")
    Retrier.retry(succeedsAfterNCallsEither(3), 3) shouldBe Right("Good")
  }

  "MonadError retrier" should "works with Option" in {
    def succeedsAfterNCallsOption(succeedAfter: Int)(n: Int): Option[String] =
      if (succeedAfter == n) Some("Good") else None

    Retrier.retry(succeedsAfterNCallsOption(3), 1) shouldBe None
    Retrier.retry(succeedsAfterNCallsOption(3), 2) shouldBe None
    Retrier.retry(succeedsAfterNCallsOption(3), 3) shouldBe Some("Good")
  }

  "MonadError retrier" should "works with IO" in {
    def succeedsAfterNCallsIO(succeedAfter: Int)(n: Int): IO[String] =
      if (succeedAfter == n) IO.pure("Good") else IO.raiseError(new Throwable("Bad"))

    runSafe(Retrier.retry(succeedsAfterNCallsIO(3), 1)) shouldBe Left("Bad")
    runSafe(Retrier.retry(succeedsAfterNCallsIO(3), 2)) shouldBe Left("Bad")
    runSafe(Retrier.retry(succeedsAfterNCallsIO(3), 3)) shouldBe Right("Good")
  }
}

object p6_MonadErrorSpec {
  import cats.effect.unsafe.implicits.global

  def runSafe[A](io: IO[A]): Either[String, A] =
    io.attempt.unsafeRunSync().leftMap(_.getMessage)
}
