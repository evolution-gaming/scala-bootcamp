import cats.effect.{ContextShift, IO, Timer}
import fs2._

import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

implicit val timer: Timer[IO] = IO.timer(ExecutionContext.parasitic)
implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

val inputStr =
  """foo bar baz
    |qux bar baz
    |foO Foo fOO """.stripMargin

// fs2.Stream has two type parameters
// Last one is element type
// First one is IO-like effect type, in which the stream will be running
val lines: Stream[IO, String] = Stream.eval(IO(inputStr)).through(fs2.text.lines)
// Terminal operation is .compile - returns a builder for the end result
lines.debug()
  .compile.drain.unsafeRunSync()

// Intermediate stages can be represented as fs2.Pipe[F, In, Out] = Stream[F, In] => Stream[F, Out]
def toWords[F[_]]: Pipe[F, String, String] =
  _.flatMap(line => Stream.iterable(line.split("\\s+")))
// .through applies a Pipe onto stream
val words = lines.through(toWords)
words.debug().compile.drain.unsafeRunSync()

val wordCounts: Stream[IO, Map[String, Int]] = words
  .map(_.toLowerCase)
  .scanMap(word => Map(word -> 1))
// Internally, fs2 passes elements in batches, represented by fs2.Chunk
// Per-element operations like map/filter transparently operate on chunks
wordCounts
  .debugChunks().debug()
  .compile.lastOrError.unsafeRunSync()


// F is not necessarily IO
// There is fs2.Pure for pure (no side-effects) streams
val pureLines: Stream[Pure, String] = Stream(inputStr).through(fs2.text.lines)

val pureCount: Option[Map[String, Int]] = pureLines
  .through(toWords)
  .map(_.toLowerCase)
  .scanMap(word => Map(word -> 1))
  .compile.last

// Other F[_]'s can work; .compile requires at least Sync
// E.g. monix Task can work well
{
  import monix.eval.Task
  import monix.execution.Scheduler

  val monixLines: Stream[Task, String] = Stream.eval(Task(inputStr))
  val monixWordCount: Task[Map[String, Int]] = monixLines
    .through(fs2.text.lines)
    .through(toWords)
    .map(_.toLowerCase()).scanMap(word => Map(word -> 1))
    .compile.lastOrError

  implicit val scheduler: Scheduler = Scheduler(ExecutionContext.parasitic)
  monixWordCount.runSyncUnsafe()
}


// Inputs

Stream(1, 2, 3)
Stream.iterable(Seq(1, 2, 3))
Stream.chunk(Chunk(1, 2, 3))

Stream.eval(IO {
  println("Side-effect!")
  42
}).compile.toVector.unsafeRunSync()

// Note: no elements, only side-effect
Stream.eval_(IO {
  println("Side-effect, with empty stream")
  42
}).compile.toVector.unsafeRunSync()

Stream.evalUnChunk(IO(Chunk(1, 2, 3)))
  .debugChunks().debug().compile.drain.unsafeRunSync()

// Ticks
Stream.fixedDelay[IO](100.millis)
  .map(_ => Instant.now())
  .debug()
  .take(5)
  .compile.drain
  .unsafeRunSync()

// Emits at fixed rate vs fixedDelay which just sleeps after each element
Stream.fixedRate[IO](100.millis)

// Processing

// Async/effects
words.evalMap(word => IO(word.toLowerCase()))
  .compile.toList.unsafeRunSync()

// Run a F[_] for each element, but don't change elements
words.evalTap(word => IO(println(s"Word: $word")))
  .compile.drain.unsafeRunSync()

// Parallel versions
words.parEvalMap(4)(word => IO(word.toLowerCase))
words.parEvalMapUnordered(4)(word => IO(word.toLowerCase))

// Beware: eval* methods break chunks
words
  .debugChunks(logger = str => println(s"Before: $str"))
  .evalMap(word => IO(word.toLowerCase))
  .debugChunks(logger = str => println(s"After: $str"))
  .compile.drain.unsafeRunSync()
// This can have noticeable effect on throughput

// Use evalMapChunk/etc... to avoid
words.evalMapChunk(word => IO(word.toLowerCase))
  .debugChunks().compile.drain.unsafeRunSync()

// Or operate directly on stream on chunks

import cats.syntax.traverse._

words
  .chunks // Stream[F, Chunk[T]]
  .evalMap(chunk => chunk.traverse(word => IO(word.toLowerCase)))
  .flatMap(Stream.chunk) // Stream[F, Chunk[T]] => Stream[F, T]
  .debugChunks().compile.drain.unsafeRunSync()

// Alternatively flatMap into Stream.evalUnChunk(F[Output chunk])
words.chunks
  .flatMap(chunk => Stream.evalUnChunk(chunk.traverse(word => IO(word.toLowerCase))))

// FSMs

words.scan("!") { case (acc, word) => acc + word.take(1) }
  .compile.toList.unsafeRunSync()

// Version using F[State]
words.evalScan("!") { case (acc, word) => IO(acc + word.take(1)) }

// Scan using Monoid.zero/Monoid.combine
words.scanMonoid
  .compile.toList.unsafeRunSync()
// With .map() before monoid
words.scanMap(word => Map(word.take(1) -> (1, Set(word))))
  .compile.lastOrError.unsafeRunSync()

// mapAccumulate has a separate type for output
def zipWithPrevious[F[_], T](in: Stream[F, T]): Stream[F, (T, T)] = {
  in.mapAccumulate(Option.empty[T]) {
    case (None, value) => Some(value) -> None
    case (Some(prev), value) => Some(value) -> Some(prev -> value)
  }.map(_._2).unNone
}

words.through(zipWithPrevious)
  .compile.toList.unsafeRunSync()

// scanChunks operates on entire chunks at once
words
  .scanChunks("!") {
    case (acc, chunk) =>
      val newAcc = acc + chunk.iterator.map(_.take(1)).mkString
      newAcc -> Chunk(newAcc)
  }
  .debugChunks().compile.drain.unsafeRunSync()

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

// broadcast
val broadcastWords: Stream[IO, Stream[IO, String]] = words.broadcast

words.take(4)
  .broadcast.take(3) // 3 streams, each sees all elements from upstream
  .map(_.map(_.toUpperCase()))
  .parJoinUnbounded
  .compile.toList.unsafeRunSync()

// balance
words
  .balance(1).take(3) // 3 load-balanced streams
  .map(_.map(_.toUpperCase()))
  .parJoinUnbounded
  .compile.toList.unsafeRunSync()

// Split stream, process branches differently
val ints = Stream.range(1, 5).covary[IO].debug()

val oddsPipe: Pipe[IO, Int, String] = _.filter(_ % 2 != 0).map(odd => s"Odd: $odd")
val evensPipe: Pipe[IO, Int, String] = _.filter(_ % 2 == 0).map(odd => s"Even: $odd")

ints.broadcastThrough(oddsPipe, evensPipe)
  .compile.toList.unsafeRunSync()

// For comparison, without broadcast
// Note how debug() on ints is evaluated twice
Stream(ints.through(oddsPipe), ints.through(evensPipe))
  .parJoinUnbounded
  .compile.toList.unsafeRunSync()

