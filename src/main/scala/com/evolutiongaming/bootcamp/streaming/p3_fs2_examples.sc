import cats.effect.{ContextShift, IO, Resource, Timer}
import cats.syntax.either._
import cats.syntax.traverse._
import fs2.concurrent.Queue
import fs2.{Chunk, Stream}
import io.circe.Json

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.Random

implicit val timer: Timer[IO]     = IO.timer(ExecutionContext.parasitic)
implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

// Poll a database, emit newly-inserted rows as stream
case class DbRow(data: Int, lastAdded: Instant)
def getItemsAfter(after: Instant): IO[Seq[DbRow]] = {
  /* Do a DB request here. SQL request can look like this
  select *
  from table
  where added_at > $after
  order by added_at ascending
  limit 100
   */
  // This just emulates data inserted at 1 row per second
  IO {
    Iterator
      .iterate(after.truncatedTo(ChronoUnit.SECONDS))(_.plusSeconds(1L))
      .drop(1)
      .takeWhile(_.isBefore(Instant.now()))
      .take(5) // at most 5 rows per request
      .map(addedAt => DbRow(Random.nextInt(), addedAt))
      .toVector
  }
}

val rowStream: Stream[IO, DbRow] =
  Stream.unfoldChunkEval(Instant.now().minusSeconds(10)) { after =>
    getItemsAfter(after).flatMap { rows =>
      if (rows.nonEmpty) { // We have data, return it along with new `after`
        val lastAdded = rows.map(_.lastAdded).max
        IO.pure(Option(Chunk.iterable(rows) -> lastAdded))
      } else { // No data, wait a bit and repeat
        IO.sleep(100.millis) *> IO.pure(Some(Chunk.empty -> after))
      }
    }
  }
// rowStream is infinite, take first 15 elements for demonstration
// Note that first 10 elements come in two chunks
//rowStream.take(15).debugChunks().compile.drain.unsafeRunSync()

// Byte streams
// fs2 can efficiently represent raw byte streams as Stream[F, Byte]
val inputStr                        =
  """foo bar baz
    |qux bar baz
    |foO Foo fOO """.stripMargin
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
  .compile
  .toList
  .unsafeRunSync()

// Parsing json-lines and other structured streams
Stream(
  """|{"foo": 42}
     |[1, 2, 3]
     |{"more":"json"}
     |""".stripMargin
).covary[IO]
  .through(fs2.text.lines)
  .filter(_.nonEmpty)
  .evalMapChunk(line => io.circe.parser.parse(line).liftTo[IO])
  .compile
  .toList
  .unsafeRunSync()
  .map(json => json.noSpaces)

// When decoding, consider dropping unparseable elements
// Otherwise: decoding fails -> stream fails -> entire app crashes
def safeDecode(line: String): IO[Option[Json]] = {
  io.circe.parser.decode[Json](line) match {
    case Right(json)           => IO.pure(Some(json))
    case Left(decodingFailure) =>
      IO {
        // Scream into logs/metrics here
        println(s"Decoding failed for $line")
        decodingFailure.printStackTrace()
        None // but return None
      }
  }
}
Stream(
  """|{"json": "ok"}
     |{"json": "ok"}
     |this is not json
     |{"other": "json"}
     |""".stripMargin
).covary[IO]
  .through(fs2.text.lines)
  .evalMapChunk(safeDecode)
  .unNone // unwrap Option
  .compile
  .toList
  .unsafeRunSync()
  .map(json => json.noSpaces)

// Side input into a stream
// There is a stream running in background, you want to insert elements there
// Solution: use a queue
val streamWithInput = for {
  input <- Resource.eval(Queue.boundedNoneTerminated[IO, Int](10))

  // Background stream
  streamFiber <- input.dequeue // this is Stream[IO, Int]
    .evalMap(value => IO(println(s"Received $value")))
    .onFinalize(IO(println("Terminating")))
    .compile
    .drain
    .background

  // Feed it some elements
  _           <- Resource.eval(
    (1 to 5).toList.traverse(toInsert => input.enqueue1(Some(toInsert)))
  )
  // Stop the stream by sending None
  _           <- Resource.eval(input.enqueue1(None))
  // Await stream end, generally not necessary
  _           <- Resource.eval(streamFiber)
} yield ()

//streamWithInput.use(_ => IO.unit).timeout(10.seconds).unsafeRunSync()

// Note: input.dequeue is Pipe[IO, Option[Int], Unit]
// This can be used in another stream (or streams)
