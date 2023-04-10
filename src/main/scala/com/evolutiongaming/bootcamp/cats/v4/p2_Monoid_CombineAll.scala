package com.evolutiongaming.bootcamp.cats.v4

object p2_Monoid_CombineAll {

  import cats.Monoid
  import cats.data.NonEmptyList

  // combineAll can be surprisingly useful
  val combineAllInts: Int = Monoid.combineAll(1 to 4) // 10

  // groupBy can be done with monoids
  val groupByViaCombineAll: Map[Int, Seq[Int]] = Monoid.combineAll {
    (1 to 10).map(value => Map(value % 3 -> Seq(value)))
  } // == Map(0 -> List(3, 6, 9), 1 -> List(1, 4, 7, 10), 2 -> List(2, 5, 8))

  // So, why use this instead of native operations on types?

  // Try aggregating this by hand
  Monoid[Map[Any, Map[Int, (Int, NonEmptyList[String])]]]

  // And a bit more realistic example

  import java.time.Instant

  type ProblemKind = String
  type Client      = String

  final case class Problem(kind: ProblemKind, client: Client)

  /* Combine a lot of Problems into this:
                                       total per kind
                                              |      problems per client (of that kind)
                                             \/               \/                       */
  type AggregatedResult = Map[ProblemKind, (Int, Map[Client, Int])]

  def aggregateCombineAll(
    log: Iterator[(Instant, Seq[Problem])]
  ): AggregatedResult = ??? // cats.Monoid.combineAll { ??? }

  // Without monoids, aggregating into that shape is already pretty cumbersome
  def aggregateManually(
    log: Iterator[(Instant, Seq[Problem])]
  ): AggregatedResult = {
    log
      .flatMap(_._2)
      .foldLeft(Map.empty[ProblemKind, (Int, Map[Client, Int])]) { case (acc, Problem(kind, client)) =>
        val (oldKindTotal, oldPerClient) = acc.getOrElse(kind, (0, Map.empty[Client, Int]))
        val newKindTotal                 = oldKindTotal + 1
        val oldProblemsForClient         = oldPerClient.getOrElse(client, 0)
        val newPerClient                 = oldPerClient.updated(client, oldProblemsForClient + 1)
        acc.updated(kind, newKindTotal -> newPerClient)
      }
  }
}
