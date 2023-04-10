// Traverse

// Let's start with original use-case in Future
import scala.concurrent.{ExecutionContext, Future}

implicit val ec = ExecutionContext.parasitic

// You have a collection of values
val values = List(1, 2, 3)

// You want to apply an asynchronous function to them
def asyncIncrement(value: Int): Future[Int] = Future.successful(value + 1)
// And you want to have results combined back into a list
type IncResult = Future[List[Int]]

// Using map won't work, that gives a list of futures
val mappedValues: List[Future[Int]] = values.map(asyncIncrement)

// Solution: Future.traverse
val incResult: IncResult = Future.traverse(values)(asyncIncrement)

// Cats' Traverse is a generalization of that idea

import cats.Traverse
import cats.syntax.traverse._

// Usage requires Traverse for outer type (List)
// and Applicative for inner type (Future)
Traverse[List].traverse(values)(asyncIncrement)
values.traverse(asyncIncrement)

// It's usable not just with futures
import cats.syntax.either._
import cats.syntax.validated._

val eithers = List(
  1.rightNel[String],
  2.rightNel[String],
)

eithers.traverse(_.map(_ + 1))
// There's sequence, equivalent to traverse(x => x)
eithers.sequence

// Validated is an applicative, and will accumulate errors with traverse
List(
  1.validNel[String],
  "nope".invalidNel[Int],
  "error".invalidNel[Int],
).sequence

// Traverse can be interpreted in a more general sense:
// It allows to exchange two nested types: F[G[_]] => G[F[_]]
// "Oh, All the things you'll traverse" by Luka Jacobowitz @ ScalaDays 2018
// https://www.youtube.com/watch?v=yEYPf44rS2U

// Bonus topic: Parallel
// Either and Validated are equivalent in structure, the difference is in typeclasses
// One has a monad, the other is limited to applicative
// Parallel captures that relationship between two types

import cats.Parallel
import cats.data.EitherNel
import cats.syntax.apply._
import cats.syntax.parallel._

// Either has a Parallel, requiring a semigroup for left type, same as Validated
Parallel[EitherNel[String, *]]

val eitherNels = List(
  1.rightNel[String],
  "nope".leftNel[String],
  "error".leftNel[String],
)

eitherNels.sequence

// Convert each Either to Validated, run sequence with Validated, convert result back to Either
eitherNels.parSequence

// Most of operations requiring Applicative also have a version for Parallel with "par" prefix
("nope".leftNel[Int], "error".leftNel[Int]).mapN(_ + _)
("nope".leftNel[Int], "error".leftNel[Int]).parMapN(_ + _)
