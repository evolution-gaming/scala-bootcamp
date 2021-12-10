import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Source}

import scala.collection.immutable.ArraySeq
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}


implicit val actorSystem: ActorSystem = ActorSystem("streams")

def debugPrint[T](stream: Source[T, _]): Unit = {
  Await.result(stream.runForeach(println), 10.seconds)
  ()
}

def await[T](value: Future[T]): T = Await.result(value, 10.seconds)

val inputStr =
  """foo bar baz
    |qux bar baz
    |foO Foo fOO """.stripMargin
val lines = Source.fromIterator(() => inputStr.linesIterator)
val words = lines.mapConcat(line => ArraySeq.unsafeWrapArray(line.split("\\s+")))


// Common case: scan, but have different output type O instead of state
// I.e. (State, Input) => (State, Output)
// Solution: Use (State, Output) in scan, process only S part from the , map output to O after
// output pairs of (previous element, current element), first output on second input
def zipWithPrevious[T]: Flow[T, (T, T), NotUsed] = {
  Flow[T].scan(Option.empty[T] -> Option.empty[(T, T)]) {
    case ((Some(prev), _), value) => Some(value) -> Some(prev -> value)
    case ((None, _), value) => Some(value) -> None
  }.collect {
    case (_, Some(output)) => output
  }
}


// Example: group words by first letter, keep only first word in group
// groupBy returns a SubFlow
// Treat SubFlow as multiple parallel substreams,
// transformations operate on all substreams at once
val groupedByFirstLetter = words.groupBy(10, _.take(1))
val firstWordPerGroup = groupedByFirstLetter
  .take(1)
  .mergeSubstreams

debugPrint {
  firstWordPerGroup
}

