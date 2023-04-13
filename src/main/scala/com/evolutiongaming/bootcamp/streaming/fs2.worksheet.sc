import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Temporal}
import fs2._

import java.time.Instant
import scala.concurrent.duration._

implicit val temporal = Temporal[IO]

// Let's start with ubiquitous word count example
// Take a string, split it into words, count each word

val inputStr =
  """foo bar baz
    |qux bar baz
    |foO Foo fOO """.stripMargin

// fs2.Stream has two type parameters
// Last one is element type
// First one is IO-like effect type, in which the stream will be running
val lines: Stream[IO, String] = Stream.eval(IO(inputStr)).through(fs2.text.lines)
// Terminal operation is .compile - returns a builder for the end result
lines.debug().compile.drain.unsafeRunSync()

// Next, split each line into words, each word is a separate element in stream
// This can be done with a flatMap:
lines
  .flatMap(line => Stream.iterable(line.split("\\s+")))
  .debug()
  .compile
  .drain
  .unsafeRunSync()

// This can also be extracted into a reusable element,
// represented as fs2.Pipe[F, In, Out] = Stream[F, In] => Stream[F, Out]
def toWords[F[_]]: Pipe[F, String, String] =
  _.flatMap(line => Stream.iterable(line.split("\\s+")))
// Use .through to apply a Pipe onto stream
val words                                  = lines.through(toWords)
words.debug().compile.drain.unsafeRunSync()

// Accumulate word counts in a Map, outputting result after each element
words
  .map(_.toLowerCase)
  .scan(Map.empty[String, Int]) { case (acc, word) =>
    acc.updated(word, acc.getOrElse(word, 0) + 1)
  }
  .debug()
  .compile
  .drain
  .unsafeRunSync()
// Map[String, Int] has a monoid, this can be written with scanMap
val wordCounts: Stream[IO, Map[String, Int]] = words
  .map(_.toLowerCase)
  .scanMap(word => Map(word -> 1))
wordCounts.debug().compile.drain.unsafeRunSync()

// And we want a final result
val finalWordCount: IO[Map[String, Int]] = wordCounts.compile.lastOrError
finalWordCount.unsafeRunSync()

// The entire pipeline, without intermediate streams
lines
  .through(toWords)
  .map(_.toLowerCase)
  .scanMap(word => Map(word -> 1))
  .compile
  .lastOrError
  .unsafeRunSync()

// Pipeline processes elements as they arrive, it doesn't store everything in memory
// Repeat input a bunch of times
lines
  .repeatN(100_000)
  .through(toWords)
  .map(_.toLowerCase)
  .scanMap(word => Map(word -> 1))
  .compile
  .lastOrError
  .unsafeRunSync()

// F is not necessarily IO
// There is fs2.Pure for pure (no side-effects) streams
val pureLines: Stream[Pure, String] = Stream(inputStr).through(fs2.text.lines)

val pureCount: Option[Map[String, Int]] = pureLines
  .through(toWords)
  .map(_.toLowerCase)
  .scanMap(word => Map(word -> 1))
  .compile
  .last

// Other F[_]'s can work
// Let's try ZIO
{
  import zio.stream.interop.fs2z._
  import zio.{Runtime, Task, UIO, ZIO, Unsafe}

  val zioLines: Stream[UIO, String]                = Stream.eval(ZIO.succeed(inputStr))
  val zioWordCount: Task[Option[Map[String, Int]]] = zioLines
    .through(fs2.text.lines)
    .through(toWords)
    .map(_.toLowerCase())
    .scanMap(word => Map(word -> 1))
    .toZStream()
    .runLast

  Unsafe.unsafe { implicit unsafe =>
    Runtime.default.unsafe.run(zioWordCount)
  }
}

// Internally fs2 actually elements in batches, represented with fs2.Chunk
// Per-element operators like map/filter work on chunks transparently
// Can be made visible with .debugChunks()
// There's also some chunk-aware operators
lines.debugChunks().compile.drain.unsafeRunSync()

words.debugChunks().debug().compile.drain.unsafeRunSync()

// Inputs

Stream(1, 2, 3)
Stream.iterable(Seq(1, 2, 3))
Stream.chunk(Chunk(1, 2, 3))

Stream
  .eval(IO {
    println("Side-effect!")
    42
  })
  .compile
  .toVector
  .unsafeRunSync()

Stream
  .evalUnChunk(IO(Chunk(1, 2, 3)))
  .debugChunks()
  .debug()
  .compile
  .drain
  .unsafeRunSync()

// Ticks
Stream
  .fixedDelay[IO](100.millis)
  .map(_ => Instant.now())
  .debug()
  .take(5)
  .compile
  .drain
  .unsafeRunSync()

// Emits at fixed rate vs fixedDelay which just sleeps after each element
Stream.fixedRate[IO](100.millis)

// unfold can occasionally be useful
// Takes State => Option[(Output, Next State)]
// There's other variants:
// unfoldChunk emits a chunk
// unfoldEval takes F[_] instead of pure function
// unfoldChunkEval for both
// Can be useful for e.g. polling a database
// Here's fibonacci sequence
val fibonacci = Stream.unfold(0 -> 1) { case (a, b) =>
  Some(a -> (b, a + b))
}
fibonacci.take(10).compile.toList

// Processing

// Async/effects
words.evalMap(word => IO(word.toLowerCase())).compile.toList.unsafeRunSync()

// Run a F[_] for each element, but don't change elements
words.evalTap(word => IO(println(s"Word: $word"))).compile.drain.unsafeRunSync()

// Parallel versions
words.parEvalMap(4)(word => IO(word.toLowerCase))
words.parEvalMapUnordered(4)(word => IO(word.toLowerCase))

// Beware: eval* methods break chunks
words
  .debugChunks(logger = str => println(s"Before: $str"))
  .evalMap(word => IO(word.toLowerCase))
  .debugChunks(logger = str => println(s"After: $str"))
  .compile
  .drain
  .unsafeRunSync()
// This can have noticeable effect on throughput

// Use evalMapChunk/etc... to avoid
words
  .evalMapChunk(word => IO(word.toLowerCase))
  .debugChunks()
  .compile
  .drain
  .unsafeRunSync()

// Or operate directly on stream of chunks

words.chunks // Stream[F, Chunk[T]]
  .evalMap(chunk => chunk.traverse(word => IO(word.toLowerCase)))
  .flatMap(Stream.chunk) // Stream[F, Chunk[T]] => Stream[F, T]
  .debugChunks()
  .compile
  .drain
  .unsafeRunSync()

// Alternatively flatMap into Stream.evalUnChunk(F[Output chunk])
words.chunks
  .flatMap(chunk => Stream.evalUnChunk(chunk.traverse(word => IO(word.toLowerCase))))

// FSMs

words.scan("!") { case (acc, word) => acc + word.take(1) }.compile.toList.unsafeRunSync()

// Version using F[State]
words.evalScan("!") { case (acc, word) => IO(acc + word.take(1)) }

// Scan using Monoid.zero/Monoid.combine
words.scanMonoid.compile.toList.unsafeRunSync()
// With .map() before monoid
words.scanMap(word => Map(word.take(1) -> (1, Set(word)))).compile.lastOrError.unsafeRunSync()

// mapAccumulate has a separate type for output
def zipWithPrevious[F[_], T](in: Stream[F, T]): Stream[F, (T, T)] = {
  in.mapAccumulate(Option.empty[T]) {
    case (None, value)       => Some(value) -> None
    case (Some(prev), value) => Some(value) -> Some(prev -> value)
  }.map(_._2)
    .unNone
}

words.through(zipWithPrevious).compile.toList.unsafeRunSync()

// scanChunks operates on entire chunks at once
words
  .scanChunks("!") { case (acc, chunk) =>
    val newAcc = acc + chunk.iterator.map(_.take(1)).mkString
    newAcc -> Chunk(newAcc)
  }
  .debugChunks()
  .compile
  .drain
  .unsafeRunSync()

// Sinks

words.compile.drain
words.compile.last
words.compile.lastOrError

words.compile.toList
words.compile.toVector
words.compile.to(Set)
words.compile.to(Chunk)

words.compile.fold(Vector.empty[String])(_.appended(_))
words.compile.foldChunks("!")(_ + _.map(_.take(1)))

// Resource instead of bare F
words.compile.resource.lastOrError

// Non-linear pipelines

// Merge two streams of same element type
Stream
  .range(0, 5)
  .covary[IO]
  .merge(Stream.range(5, 20))
  .compile
  .toList
  .unsafeRunSync()
// Note: result ends when BOTH streams end
// Use mergeHaltL/mergeHaltR/mergeHaltBoth if you want to end stream earlier

// mergeHaltL(tick stream) can be used to introduce time into FSMs
words
  .map(Right(_))
  .mergeHaltL(Stream.fixedDelay(1.second).map(Left(_)))
  .scan(()) {
    case (acc, Right(data)) => acc // Do actions on new data
    case (acc, Left(_))     => acc // Do actions on tick
  }

// parJoinUnbounded merges a stream of streams
val dynamicStream: Stream[IO, Stream[IO, Int]] = Stream(
  Stream.range(0, 5).covary[IO],
  Stream.range(10, 15).covary[IO],
  Stream.range(20, 25).covary[IO],
)
dynamicStream.parJoinUnbounded.compile.toList.unsafeRunSync()
// parJoinUnbounded will try to read all streams at once
// there is parJoin(Int), which limits how much streams are open at once
dynamicStream.parJoin(2).compile.toList.unsafeRunSync()

// broadcast

// Split stream, process branches differently
val ints = Stream.range(1, 5).covary[IO].debug()

// Each branch selects elements it wants and does further processing
val oddsPipe: Pipe[IO, Int, String]  = _.filter(_ % 2 != 0).map(odd => s"Odd: $odd")
val evensPipe: Pipe[IO, Int, String] = _.filter(_ % 2 == 0).map(odd => s"Even: $odd")

ints.broadcastThrough(oddsPipe, evensPipe).compile.toList.unsafeRunSync()

// For comparison, without broadcast
// Note how debug() on ints is evaluated twice
Stream(ints.through(oddsPipe), ints.through(evensPipe)).parJoinUnbounded.compile.toList.unsafeRunSync()

/* Explore MORE
 * - throttling/debouncing
 * - resource handling
 * - errors handling
 * - interruption
 * on https://fs2.io/#/guide */
