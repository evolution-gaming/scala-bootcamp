import zio.stream.{UStream, ZPipeline, ZSink, ZStream}
import zio.{Chunk, Runtime, UIO, Unsafe, ZIO, durationInt}

import java.time.Instant

implicit class ZIOOps[A](val zio: UIO[A]) {
  def unsafeRun(): A = Unsafe.unsafe { implicit unsafe =>
    Runtime.default.unsafe.run(zio).getOrThrow()
  }
}

val inputStr =
  """foo bar baz
    |qux bar baz
    |foO Foo fOO """.stripMargin

// Creating a stream of lines
// UStream[String] is ZStream[Any, Nothing, String]
val lines: UStream[String] = ZStream.succeed(inputStr).via(ZPipeline.splitLines)

lines.debug.runDrain.unsafeRun()

// Next, split each line into words, each word is a separate element in stream
// This can be done with a flatMap:
lines
  .flatMap(line => ZStream.fromIterable(line.split("\\s+")))
  .debug
  .runDrain
  .unsafeRun()

// This can also be extracted into a reusable ZPipeline
val toWords: ZPipeline[Any, Nothing, String, String] = ZPipeline.fromFunction {
  _.flatMap(line => ZStream.fromIterable(line.split("\\s+")))
}

// Use .via to apply ZPipeline to a stream
val words = lines.via(toWords)
words.debug.runDrain.unsafeRun()

// Accumulate word counts in a Map, outputting result after each element
val wordCounts: UStream[Map[String, Int]] = words
  .map(_.toLowerCase)
  .scan(Map.empty[String, Int]) { case (acc, word) =>
    acc.updated(word, acc.getOrElse(word, 0) + 1)
  }
wordCounts.debug.runDrain.unsafeRun()

// And we want a final result
val finalWordCount: UIO[Option[Map[String, Int]]] = wordCounts.run(ZSink.last)
finalWordCount.unsafeRun()

// The entire pipeline, without intermediate streams
lines
  .via(toWords)
  .map(_.toLowerCase)
  .scan(Map.empty[String, Int]) { case (acc, word) =>
    acc.updated(word, acc.getOrElse(word, 0) + 1)
  }
  .runLast
  .unsafeRun()

// Pipeline processes elements as they arrive, it doesn't store everything in memory
// Repeat input a bunch of times
def repeatN[A](stream: UStream[A], n: Int): UStream[A] = if (n > 1) stream ++ repeatN(stream, n - 1) else stream
//repeatN(lines, 100_000)
//  .via(toWords)
//  .map(_.toLowerCase)
//  .scan(Map.empty[String, Int]) { case (acc, word) =>
//    acc.updated(word, acc.getOrElse(word, 0) + 1)
//  }
//  .runLast
//  .unsafeRun()

// Internally ZStream also elements in batches, represented with zio.Chunk
// Per-element operators like map/filter work on chunks transparently
// Can be made visible with .chunks()
// There's also some chunk-aware operators
lines.chunks.debug.runDrain.unsafeRun()

words.chunks.debug.mapConcatChunk(identity).debug.runDrain.unsafeRun()

// Inputs
ZStream.succeed("single element")
ZStream.fail("error")
ZStream.fromIterable(List(1, 2, 3))
ZStream.fromChunk(Chunk(1, 2, 3))
ZStream.fromFileName("file.txt")
ZStream.fromZIO(ZIO.succeed { println("Side-effect!"); 42 }).runCollect.unsafeRun()
// and others...

// Ticks
ZStream.tick(100.millis).mapZIO(_ => ZIO.succeed { Instant.now() }).debug.take(5).runDrain.unsafeRun()

// Produces value until it's None
val fibonacci = ZStream.unfold(0 -> 1) { case (a, b) =>
  Some(a -> (b, a + b))
}
fibonacci.take(10).run(ZSink.collectAll).unsafeRun()

// Processing

// Async/effects
words.mapZIO(word => ZIO.succeed(word.toLowerCase())).runCollect.unsafeRun()

// Run a F[_] for each element, but don't change elements
words.tap(word => ZIO.succeed(println(s"Word: $word"))).runCollect.unsafeRun()

// Parallel versions
words.mapZIOPar(4)(word => ZIO.succeed(word.toLowerCase))
words.mapZIOParUnordered(4)(word => ZIO.succeed(word.toLowerCase))

// Beware: eval* methods break chunks
words.chunks
  .debug(s"Before")
  .mapConcatChunk(identity)
  .mapZIO(word => ZIO.succeed(word.toLowerCase))
  .chunks
  .debug(s"After")
  .mapConcatChunk(identity)
  .runDrain
  .unsafeRun()
// This can have noticeable effect on throughput

// Operate directly on stream of chunks
words // Stream[F, Chunk[T]]
  .mapChunksZIO(chunk => ZIO.foreach(chunk)(word => ZIO.succeed(word.toLowerCase)))
  .chunks
  .debug(s"After")
  .runDrain
  .unsafeRun()

// FSMs

words.scan("!") { case (acc, word) => acc + word.take(1) }.runCollect.unsafeRun()
// Version using F[State]
words.scanZIO("!") { case (acc, word) => ZIO.succeed(acc + word.take(1)) }

// mapAccumulate has a separate type for output
def zipWithPrevious[T]: ZPipeline[Any, Nothing, T, (T, T)] = ZPipeline.fromFunction {
  _.mapAccum(Option.empty[T]) {
    case (None, value)       => Some(value) -> None
    case (Some(prev), value) => Some(value) -> Some(prev -> value)
  }.collect { case Some(value) =>
    value
  }
}

words.via(zipWithPrevious).runCollect.unsafeRun()

// Sinks

words.runDrain
words.runLast // the same as words.run(ZSink.last)
words.run(ZSink.foldLeft(0)((curr, next) => curr + next.length))
words.run(ZSink.foldChunks("!")(_ => true)(_ + _.map(_.take(1))))

words.runCollect
words.run(ZSink.collectAllN(5))
words.run(ZSink.collectAllToSet)
words.run(ZSink.collectAllToSetN(3))
words.run(ZSink.collectAllToMap[String, String](identity)(_ + _))

// Non-linear pipelines
// Merge two streams of same element type
ZStream
  .range(0, 5)
  .merge(ZStream.range(5, 20))
  .runCollect
  .unsafeRun()
// Note: result ends when BOTH streams end
// Use mergeHaltLeft/mergeHaltRight if you want to end stream earlier

// mergeHaltL(tick stream) can be used to introduce time into FSMs
words
  .map(Right(_))
  .mergeHaltLeft(ZStream.tick(1.second).map(Left(_)))
  .scan(()) {
    case (acc, Right(data)) => acc // Do actions on new data
    case (acc, Left(_))     => acc // Do actions on tick
  }

// flattenPar merges a stream of streams
val dynamicStream: UStream[UStream[Int]] = ZStream.fromIterable(
  Seq(
    ZStream.range(0, 5),
    ZStream.range(10, 15),
    ZStream.range(20, 25),
  )
)
dynamicStream.flattenParUnbounded().runCollect.unsafeRun()
// flattenParUnbounded will try to read all streams at once
// there is flattenPar(Int), which limits how much streams are open at once
// outputBuffer is used to automatically prefetch elements
dynamicStream.flattenPar(2).runCollect.unsafeRun()

// broadcast
val broadcastWords = words.take(4).broadcast(3, 3)

ZIO
  .scoped {
    broadcastWords.flatMap { streams =>
      ZStream.fromChunk(streams.map(_.map(_.toUpperCase()))).flattenParUnbounded().runCollect
    }
  }
  .unsafeRun()

// Split stream, process branches differently
val ints = ZStream.range(1, 5).debug

// Each branch selects elements it wants and does further processing
val oddsPipe: ZPipeline[Any, Nothing, Int, String]  =
  ZPipeline.fromFunction(_.filter(_ % 2 != 0).map(odd => s"Odd: $odd"))
val evensPipe: ZPipeline[Any, Nothing, Int, String] =
  ZPipeline.fromFunction(_.filter(_ % 2 == 0).map(odd => s"Even: $odd"))

ZIO
  .scoped {
    ints.broadcast(2, 3).flatMap {
      case Chunk(first, second) =>
        ZStream.fromIterable(Seq(first.via(oddsPipe), second.via(evensPipe))).flattenParUnbounded().runCollect
      case other                => ZIO.dieMessage(s"expected Chunk with 2 elements, got $other")
    }
  }
  .unsafeRun()

// For comparison, without broadcast
// Note how debug() on ints is evaluated twice
ZStream(ints.via(oddsPipe), ints.via(evensPipe)).flattenParUnbounded().runCollect.unsafeRun()

/* Explore MORE
 * - throttling/debouncing
 * - resource handling
 * - errors handling
 * - interruption
 * on https://zio.dev/reference/stream/ */
