import cats.Traverse.ops.toAllTraverseOps
import cats.implicits.toTraverseOps
import zio.stream.{UStream, ZPipeline, ZSink, ZStream}
import zio.{Chunk, Runtime, UIO, Unsafe, ZIO, durationInt}

import java.time.Instant

implicit class ZIOOps[A](val zio: UIO[A]) extends AnyVal {
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
repeatN(lines, 100_000)
  .via(toWords)
  .map(_.toLowerCase)
  .scan(Map.empty[String, Int]) { case (acc, word) =>
    acc.updated(word, acc.getOrElse(word, 0) + 1)
  }
  .runLast
  .unsafeRun()

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