import cats.effect.concurrent.Ref
import cats.effect.{ContextShift, IO, Resource, Timer}
import cats.syntax.traverse._
import fs2.concurrent.Queue
import fs2.{Chunk, Stream}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

implicit val timer: Timer[IO] = IO.timer(ExecutionContext.parasitic)
implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

val inputStr =
  """foo bar baz
    |qux bar baz
    |foO Foo fOO """.stripMargin


// Resource with IO[Int], which returns "time" when run
// "time" is updated by concurrent stream running in background
// Background stream will keep running while resource is allocated
def stopwatch: Resource[IO, IO[Int]] = {
  Stream.eval(Ref[IO].of(0)).flatMap { ref =>
    val updates = Stream.fixedRate(10.millis).evalMap(_ => ref.update(_ + 1))
    Stream.emit(ref.get).concurrently(updates)
  }
    .compile.resource.lastOrError
}

stopwatch.use { getTime =>
  val printTime = getTime.flatMap(time => IO(println(s"Now: $time")))
  printTime *> IO.sleep(100.millis) *> printTime
}
//  .unsafeRunSync()


// Byte streams
// fs2 can efficiently represent raw byte streams
val networkStream: Stream[IO, Byte] =
Stream.iterable(inputStr.getBytes).chunkLimit(5).flatMap(Stream.chunk)

// Actual contents, chunk boundaries are not the same as word/line boundaries
networkStream.debugChunks().compile.drain.unsafeRunSync()

// Can still be converted to words
networkStream
  .through(fs2.text.utf8Decode)
  .debugChunks(formatter = c => s"text: $c")
  .through(fs2.text.lines)
  .debugChunks(formatter = c => s"line: $c")
  .mapChunks(_.flatMap(line => Chunk.array(line.split("\\s+"))))
  .debugChunks(formatter = c => s"word: $c")
  .compile.toList.unsafeRunSync()


// Parsing json-lines

import _root_.io.circe.parser
import cats.syntax.either._

Stream(
  """|
     |{"foo": 42}
     |[1, 2, 3]
     |
     |""".stripMargin
).covary[IO]
  .through(fs2.text.lines)
  .filter(_.nonEmpty)
  .evalMapChunk(line => parser.parse(line).liftTo[IO])
  .compile.toList.unsafeRunSync().map(json => json.noSpaces)


// Streams can be decoupled with queues
// I.e. some streams write to a queue, some streams read from the same queue
// If queue works with cats-effect (.enqueue is F[Unit]), this will also transmit back-pressure
// fs2.concurrent.Queue also integrates with fs2, e.g. dequeue is a stream
// Alternate options:
// * MVar from cats-effect works as one-item queue
// * monix-catnap has ConcurrentQueue and ConcurrentChannel
val queueSample: Resource[IO, IO[Unit]] = for {
  input <- Resource.eval(Queue.boundedNoneTerminated[IO, Int](10))
  fanOut <- Resource.eval(Queue.boundedNoneTerminated[IO, String](10))

  // Take from input, convert to String, put into fanOut
  _ <- input.dequeue
    .map(_.toString)
    .noneTerminate
    .evalMap(fanOut.enqueue1)
    .compile.drain.background

  // Two consumers from fanOut
  consumer1 <- fanOut.dequeue
    .evalMap(str => IO(println(s"Consumer1: $str")))
    .compile.drain.background
  consumer2 <- fanOut.dequeue
    .evalMap(str => IO(println(s"Consumer2: $str")))
    .compile.drain.background

  // Put actual elements into input, no streams involved
  _ <- Resource.eval(
    (1 to 10).toVector.traverse(i => input.enqueue1(Some(i))) *> input.enqueue1(None)
  )
} yield consumer1 *> consumer2

queueSample
  .use(waits => waits) // wait until two consumers complete
  .timeout(10.seconds)
//  .unsafeRunSync()

