// Semigroup for type A is an associative operation, called `combine`
// Associative: (a combine (b combine c)) <-> ((a combine b) combine c)
// Operation is also often called "add"

import cats.Semigroup

// Semigroup for Int with addition
val sampleIntSemigroup: Semigroup[Int] = new Semigroup[Int] {
  override def combine(x: Int, y: Int) = x + y
}

// Monoid[A] is semigroup with identity element, called "empty", also often referred as "zero"
// (empty combine a) <-> a
// (a combine empty) <-> a

import cats.Monoid

// Sample monoid for strings
val sampleStringMonoid: Monoid[String] = new Monoid[String] {
  override def combine(x: String, y: String) = x + y

  override def empty = ""
}

// Some types can form only a semigroup
// E.g there's no empty value for NonEmptyList

import cats.data.NonEmptyList

def sampleNelSemigroup: Semigroup[NonEmptyList[Int]] = _ ++ _.toList

// There can be multiple possible monoids for the same type,
// typeclass defines the "official" one.
// Ex 1: What's the other common associative operation on integers?

// Many other types have monoids
Monoid[String]
Monoid[Seq[Any]]
Monoid[Map[Any, String]]

// And there's some syntax sugar

import cats.syntax.monoid._

1 combine 2
1 |+| 2

import cats.syntax.option._

1.some |+| none
1.some |+| 2.some

// combineAll can be surprisingly useful
Monoid.combineAll(1 to 4)

// groupBy can be done with monoids
Monoid.combineAll {
  (1 to 10).map(value => Map(value % 3 -> Seq(value)))
}

// So, why use this instead of native operations on types?

// Try aggregating this by hand
Monoid[Map[Any, Map[Int, (Int, NonEmptyList[String])]]]

// And a bit more realistic example

import java.time.Instant
import scala.util.Random

type ProblemKind = String
type Client      = String

case class Problem(kind: ProblemKind, client: Client)

/* Combine a lot of Problems into this:
                                     total per kind
                                            |      problems per client (of that kind)
                                           \/               \/                       */
type AggregatedResult = Map[ProblemKind, (Int, Map[Client, Int])]

def aggregateProblems(log: Iterator[(Instant, Seq[Problem])]): AggregatedResult = ???

// Without monoids, aggregating into that shape is already pretty cumbersome
def aggregateManually(log: Iterator[(Instant, Seq[Problem])]): AggregatedResult = {
  log.flatMap(_._2).foldLeft(Map.empty[ProblemKind, (Int, Map[Client, Int])]) { case (acc, Problem(kind, client)) =>
    val (oldKindTotal, oldPerClient) = acc.getOrElse(kind, (0, Map.empty[Client, Int]))
    val newKindTotal                 = oldKindTotal + 1
    val oldProblemsForClient         = oldPerClient.getOrElse(client, 0)
    val newPerClient                 = oldPerClient.updated(client, oldProblemsForClient + 1)
    acc.updated(kind, newKindTotal -> newPerClient)
  }
}

val random = new Random(42)

val problems = random
  .shuffle {
    for {
      kind   <- Vector("it_broke", "works_for_me", "out_of_magic_smoke")
      client <- Vector("important", "maybe_important")
      amount  = random.between(2, 20)

      problem <- Vector.fill(amount)(Problem(kind, client))
    } yield problem
  }
  .grouped(5)
  .map(Instant.now().minusSeconds(Random.nextInt(100)) -> _)
  .toVector

problems.foreach(println)

aggregateManually(problems.iterator).mkString("\n")

aggregateProblems(problems.iterator).mkString("\n")
