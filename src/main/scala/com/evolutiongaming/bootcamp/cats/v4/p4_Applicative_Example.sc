import cats.Applicative
import cats.effect.unsafe.implicits.global
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
// Either[NonEmptyList[String], _] for contrast fail fast
wrapSum("nope".leftNel[Int], "also nope".leftNel[Int])

// Future
import scala.concurrent.{ExecutionContext, Future}

implicit val ec = ExecutionContext.parasitic

val futureSum = wrapSum(
  Future { println("Computing 1"); 1 },
  Future { println("Computing 2"); 2 },
)
futureSum.foreach(println)

// IO
import cats.effect.IO
val ioSum = wrapSum(IO(1), IO(2))
ioSum.unsafeRunSync()
