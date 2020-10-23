package com.evolutiongaming.bootcamp.cats.v2

import cats.ApplicativeError
import cats.effect.IO

object p6_MonadError {

  /**
    * Continuing discussion about abstracting over an effect type, we may want to describe a computation that
    * may fail with a certain type of error.
    * So far to express ourselves we used Either.
    * But what if we don't want to opt-in to the Either type in service definition?
    * MonadError (or ApplicativeError if we don't need flatMap) may help us.
    * MonadError has a number of handy methods such as `raise` that lifts an error to the effect context.
    */
  import cats.syntax.applicativeError._

  /**
    * Consider a naive Retrier that can run with Either, Option, IO as an effect and makes `n` attempts to complete an effectful operation f.
    * After it hits maxAttempts` it gives up and raises an error to the callee.
    */
  object Retrier {
    private def _retry[F[_], E, A](
      f: Int => F[A],
      numAttempts: Int,
      maxAttempts: Int
    )(implicit AE: ApplicativeError[F, E]): F[A] = f(numAttempts).handleErrorWith { error =>
      if (numAttempts < maxAttempts) _retry(f, numAttempts + 1, maxAttempts)
      else AE.raiseError(error)
    }

    def retry[F[_], E, A](f: Int => F[A], maxAttempts: Int)(
      implicit AE: ApplicativeError[F, E]
    ): F[A] = _retry(f, 0, maxAttempts)
  }

  type ExampleEffEither[A] = Either[String, A]
  type ExampleEffOption[A] = Option[A]
  type ExampleEffIO[A] = IO[A]

  def succeedsAfterNCallsEither(succeedAfter: Int)(n: Int): ExampleEffEither[String] =
    if (succeedAfter == n) Right("Good") else Left("Bad")

  def succeedsAfterNCallsOption(succeedAfter: Int)(n: Int): ExampleEffOption[String] =
    if (succeedAfter == n) Some("Good") else None

  def succeedsAfterNCallsIO(succeedAfter: Int)(n: Int): ExampleEffIO[String] =
    if (succeedAfter == n) IO.pure("Good") else IO.raiseError(new Throwable("Bad"))
}
