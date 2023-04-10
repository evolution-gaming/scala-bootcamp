// MonadError and ApplicativeError

import cats.ApplicativeError
import cats.data.{EitherNel, NonEmptyList, ValidatedNel}
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._

import scala.util.control.NoStackTrace

// Those add raising and handling of errors to Applicative/Monad
// E.g. this is Either[String, Int], with Left("error")
val leftError = "error".raiseError[Either[String, *], Int]

// Let's try an old example with time validation, and try it with ApplicativeError

// On dedicated error types: those can extend exception and still be sealed
// Sealed can give all benefits of an ADT, e.g. exhaustive matching
// NoStackTrace disables filling stacktrace, the "expensive" part about using exceptions
// E.g. this is used in Circe, where decoders return Either[DecodingFailure, T]
// DecodingFailure extends io.circe.Error, which is a sealed class extending Exception
sealed abstract class InvalidTime extends Exception with NoStackTrace
case class InvalidHour()          extends InvalidTime
case class InvalidMinute()        extends InvalidTime

case class Time(hour: Int, minute: Int)
object Time {
  def of[F[_]](hour: Int, minute: Int)(implicit F: ApplicativeError[F, NonEmptyList[InvalidTime]]): F[Time] = {
    def checkRange(range: Range, value: Int, error: => InvalidTime): F[Int] = {
      if (range.contains(value)) value.pure
      else NonEmptyList.one(error).raiseError
    }

    val validHour   = checkRange(0 to 23, hour, InvalidHour())
    val validMinute = checkRange(0 to 59, minute, InvalidMinute())

    (validHour, validMinute).mapN(Time.apply)
  }
}

Time.of[ValidatedNel[InvalidTime, *]](23, 59)
Time.of[ValidatedNel[InvalidTime, *]](23, -1)
Time.of[ValidatedNel[InvalidTime, *]](-1, -1)

Time.of[EitherNel[InvalidTime, *]](23, 59)
Time.of[EitherNel[InvalidTime, *]](23, -1)
Time.of[EitherNel[InvalidTime, *]](-1, -1)

// MonadThrow is MonadError with error type fixed to Throwable
// There's also ApplicativeThrow

import cats.MonadThrow

def mkTimeThrow[F[_]](hour: Int, minute: Int)(implicit F: MonadThrow[F]) = {
  for {
    _ <- F.unlessA((0 to 23).contains(hour))(InvalidHour().raiseError)
    _ <- InvalidMinute().raiseError.unlessA(0 to 59 contains minute)
  } yield Time(hour, minute)
}

import scala.util.Try

mkTimeThrow[Try](23, 59)
mkTimeThrow[Try](23, -1)
mkTimeThrow[Try](-1, -1)

// Common operations

// attempt converts F[A] to always-successful F[Either[E, A]]
val tryFailure = mkTimeThrow[Try](23, -1)
tryFailure.attempt
// rethrow is reverse of attempt, only on MonadError
tryFailure.attempt.rethrow

val midnight = Time(0, 0)

// recover takes a PartialFunction from error to value
// There's handleError taking a total function
// There's also recoverWith/handleErrorWith taking F[A] instead of A
tryFailure.recover { case _: InvalidTime =>
  midnight
}
