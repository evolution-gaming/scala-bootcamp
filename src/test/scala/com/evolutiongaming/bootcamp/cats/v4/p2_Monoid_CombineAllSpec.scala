package com.evolutiongaming.bootcamp.cats.v4

import com.evolutiongaming.bootcamp.cats.v4.p2_Monoid_CombineAll.{AggregatedResult, Problem}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.Instant
import scala.util.Random

class p2_Monoid_CombineAllSpec extends AnyWordSpec with Matchers {

  "Combine all" should {
    "be correctly combining elements" in {
      p2_Monoid_CombineAll.combineAllInts shouldBe 10
    }

    "be correctly grouping elements" in {
      p2_Monoid_CombineAll.groupByViaCombineAll shouldBe
        Map(0 -> List(3, 6, 9), 1 -> List(1, 4, 7, 10), 2 -> List(2, 5, 8))
    }
  }

  "AggregatedResult" should {

    "be correctly aggregating elements" in {
      val random = new Random(42)

      val problems: Seq[(Instant, Vector[Problem])] =
        random
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

      def sorted(result: AggregatedResult) =
        result.toList.sortBy(_._1).map { case (kind, (total, values)) =>
          (kind, (total, values.toList.sortBy(_._1)))
        }

      val aggregatedManually =
        sorted(p2_Monoid_CombineAll.aggregateManually(problems.iterator))

      val aggregatedCombineAll =
        sorted(p2_Monoid_CombineAll.aggregateCombineAll(problems.iterator))

      aggregatedManually shouldBe aggregatedCombineAll
    }
  }
}
