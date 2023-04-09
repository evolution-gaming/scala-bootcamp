import cats.Functor

// Functor is fine, but what if we want to combine multiple values?
// Applicative allows that
trait AltApplicative[F[_]] extends Functor[F] {
  def map2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C]

  // Applicative also allows putting values into F
  def pure[A](a: A): F[A]

  // Functor can be implemented with map2 and pure
  override def map[A, B](fa: F[A])(f: A => B): F[B] = map2(fa, pure(()))((a, _) => f(a))
}

// AltApplicative is an alternative definition of Applicative, let's switch to cats' one
import cats.Applicative

// Cats Applicative is defined with `ap` instead of `map2`, but they can implemented with each other
def altApplicativeToCats[F[_]](F: AltApplicative[F]): Applicative[F] = new Applicative[F] {
  override def pure[A](x: A): F[A] = F.pure(x)

  override def ap[A, B](ff: F[A => B])(fa: F[A]): F[B] = F.map2(ff, fa)((f, a) => f(a))
}

import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.option._
import cats.syntax.validated._

// Let's try adding two numbers in various F[_]s
def wrapSum[F[_]: Applicative](fa: F[Int], fb: F[Int]): F[Int] =
  (fa, fb).mapN(_ + _)

// Option[_]
wrapSum(1.some, 2.some)
wrapSum(1.some, none[Int])
wrapSum(none[Int], none[Int])

// Either[String, _]
wrapSum(1.asRight[String], 2.asRight)
wrapSum(1.asRight[String], "nope".asLeft[Int])
wrapSum("nope".asLeft[Int], "also nope".asLeft[Int])

// ValidatedNel[String, _], aka Validated[NonEmptyList[String], _]
wrapSum(1.validNel[String], 2.validNel[String])
wrapSum("nope".invalidNel[Int], 1.validNel[String])

wrapSum("nope".invalidNel[Int], "also nope".invalidNel[Int])
// Either[NonEmptyList[String], _] for contrast
wrapSum("nope".leftNel[Int], "also nope".leftNel[Int])

// Future
import scala.concurrent.{ExecutionContext, Future}

implicit val ec = ExecutionContext.parasitic

val futureSum = wrapSum(
  Future {
    println("Computing 1"); 1
  },
  Future {
    println("Computing 2"); 2
  },
)
futureSum.foreach(println)

// IO
import cats.effect.IO
val ioSum = wrapSum(IO(1), IO(2))
ioSum.unsafeRunSync()

// Common operations
// mapN
(Option(1), Option(2), Option(3)).mapN(_ + _ + _)

// *>, also known as productR
// Evaluates both args, keeps value from the right one
// Commonly used for IO-like effect types
val loggingOne = IO(println("Computing!")) *> IO(1)
loggingOne.unsafeRunSync()
// It can be used outside of IO, it's just rarely useful there
none[Int] *> 2.some
