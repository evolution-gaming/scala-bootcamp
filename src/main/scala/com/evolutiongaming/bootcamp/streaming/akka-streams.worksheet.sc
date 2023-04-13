import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{FlowShape, KillSwitches, OverflowStrategy, UniqueKillSwitch}

import scala.collection.immutable.ArraySeq
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

// Let's start with ubiquitous word count example
// Take a string, split it into words, count each word

val inputStr =
  """foo bar baz
    |qux bar baz
    |foO Foo fOO """.stripMargin

// ActorSystem is needed to run akka-streams stream
implicit val actorSystem: ActorSystem = ActorSystem("streams")

// Helper to print each element in a stream
def debugPrint[T](stream: Source[T, _]): Unit = {
  Await.result(stream.runForeach(println), 10.seconds)
  ()
}

def await[T](value: Future[T]): T = Await.result(value, 10.seconds)

// Lines from source string
// Second parameter (NotUsed in this case) tracks end result of a stream
// NotUsed is a placeholder value here
val lines: Source[String, NotUsed] = Source.fromIterator(() => inputStr.linesIterator)
debugPrint(lines)

// Split line into words
val toWords: Flow[String, String, NotUsed] =
  Flow[String].mapConcat { str =>
    ArraySeq.unsafeWrapArray(str.split("\\s+"))
  }

// Flows can be combined with Sources or other Flows with "via"
val words: Source[String, NotUsed] = lines.via(toWords)
debugPrint(words)

import cats.syntax.monoid._

// Accumulate word counts in a Map, outputting result after each element
val collectWords: Flow[String, Map[String, Int], NotUsed] =
  Flow[String]
    .map(_.toLowerCase)
    .scan(Map.empty[String, Int]) { case (acc, word) =>
      acc combine Map(word -> 1)
    }

val splitAndCount: Flow[String, Map[String, Int], NotUsed] = toWords.via(collectWords)

val wordCounts: Source[Map[String, Int], NotUsed] = words.via(collectWords)
debugPrint(wordCounts)

// Sink, takes word counts, *materializes* into Future with last element
// Note that second type parameter is different from NotUsed this time
val lastElement: Sink[Map[String, Int], Future[Map[String, Int]]] = Sink.last[Map[String, Int]]

// Combine Source and Sink into graph that can be run
// toMat: we need to combine materialized values of Source (NotUsed), and Sink(Future[...])
// Last argument is a function to combine those two: (NotUsed, Future[...]) => ...
// We only care about Future, shorthand Keep.right is useful here. (_, future) => future also works
val graph: RunnableGraph[Future[Map[String, Int]]] =
  wordCounts.toMat(lastElement)(Keep.right)

// Usually stage combinators (like via) keep the left argument; use *Mat for overrides
val kindOfUseless: RunnableGraph[NotUsed] = wordCounts.to(lastElement)

// Finally, run the graph. This needs ActorSystem
val result: Future[Map[String, Int]] = graph.run()
await(result)

// There's Source#runWith: combine Source with Sink using sink's materialized value, and run
await {
  lines.via(toWords).via(collectWords).runWith(Sink.last)
}

// Example with potentially infinite streams
// Source.cycle will repeat data from inputStr until explicitly stopped
val lotsOfData: RunnableGraph[(UniqueKillSwitch, Future[Map[String, Int]])] =
  Source
    .cycle(() => inputStr.linesIterator)
    .viaMat(KillSwitches.single)(Keep.right) // Allows to stop stream externally
    .via(splitAndCount)
    .toMat(Sink.last)(Keep.both) // We need both KillSwitch and stream result

def runLots(): Map[String, Int] = {
  val (stop, resultFuture) = lotsOfData.run()
  actorSystem.scheduler.scheduleOnce(500.millis) {
    stop.shutdown()
  }(actorSystem.dispatcher)

  await(resultFuture)
}
//runLots()

// Inputs

Source.single(42)
Source(Seq(1, 2, 3))

Source.future(Future.successful(42))
Source.tick(initialDelay = 1.second, interval = 10.seconds, tick = "tick")

// unfold can be useful
// Takes State => Option[(Next State, Output)]
// There's unfoldAsync, unfoldResource, ... Can be used for e.g. polling a database
val fib = Source.unfold(0 -> 1) { case (a, b) =>
  Some((b, a + b) -> a)
}
debugPrint(fib.take(10))

// Materializes into a queue. Elements inserted into that queue go into stream
val external: Source[Int, SourceQueueWithComplete[Int]] =
  Source.queue[Int](10, OverflowStrategy.backpressure)

// Processing

// Async transformations
words.mapAsync(2)(word => Future.successful(word.toLowerCase))
words.mapAsyncUnordered(2)(word => Future.successful(word.toLowerCase))

// FSMs and stateful transformations
// You've already seen scan
// There's scanAsync, f returns Future[Output]
words.scanAsync(Map.empty[String, Int]) { case (acc, word) =>
  Future.successful(acc combine Map(word -> 1))
}

// statefulMapConcat allows processing with mutable state
def zipWithPrevious2[T]: Flow[T, (T, T), NotUsed] = {
  Flow[T].statefulMapConcat { () =>
    var prev = Option.empty[T] // Note: this is a var

    value => {
      val result = prev match {
        case Some(prev) => Seq(prev -> value)
        case None       => Seq()
      }
      prev = Some(value)
      result
    }
  }
}
debugPrint(words.via(zipWithPrevious2))

// Outputs

Sink.ignore

Sink.last[Int]
Sink.lastOption[Int]
Sink.head[Int]
Sink.headOption[Int]

Sink.foreach[Int](println)
Sink.foreachAsync[Int](3)(i => Future.successful(println(i)))

Sink.seq[Int]
val collectedWords = await {
  words.runWith(Sink.seq)
}

// Print each element within pipeline, useful for debugging
// .wireTap can also take any sink
await {
  words
    .wireTap(println(_))
    .map(_.toLowerCase)
    .runWith(Sink.fold(Set.empty[String])(_ + _))
}

// Two sinks, words + counts
// Difference between alsoTo and wireTap: alsoTo can backpressure, wireTap will skip elements
await {
  words
    .map(_.toLowerCase)
    .alsoToMat(Sink.seq)(Keep.right)
    .via(collectWords)
    .toMat(Sink.last)(_ zip _)
    .run()
}

// Non-linear pipelines
// Merges

val int123  = Source(Seq(1, 2, 3))
val int4567 = Source(Seq(4, 5, 6, 7))

// Merge combines two streams of same type, ordering not guaranteed
debugPrint(int123.merge(int4567))

debugPrint(int123.zip(int4567))

debugPrint(int123.zipAll(int4567, -1, -2))

val dualTicks = {
  val left  = int123.throttle(1, 20.millis).map(l => s"l:$l")
  val right = int4567.throttle(1, 30.millis).map(r => s"r:$r")
  left.zipLatest(right)
}
debugPrint(dualTicks)

// More involved non-linear pipelines generally require GraphDSL
// Docs: https://doc.akka.io/docs/akka/current/stream/stream-graphs.html
// Example: incoming integers, process odds and evens differently, merge outputs
val processEven = Flow[Int].filter(_ % 2 == 0).map(even => s"Even: $even")
val processOdd  = Flow[Int].filter(_ % 2 != 0).map(odd => s"Odd: $odd")

val splitOddEvens: Flow[Int, String, NotUsed] = Flow.fromGraph {
  GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val broadcast = builder.add(Broadcast[Int](2))
    val merge     = builder.add(Merge[String](2))

    broadcast.out(0) ~> processEven ~> merge.in(0)
    broadcast.out(1) ~> processOdd ~> merge.in(1)

    // in --> broadcast --> processEven --> merge --> out
    //                 \--> processOdd  -->/

    FlowShape(broadcast.in, merge.out)
  }
}

debugPrint {
  Source(1 to 10).via(splitOddEvens)
}

// Not covered: custom operators aka GraphStage
// https://doc.akka.io/docs/akka/current/stream/stream-customize.html
