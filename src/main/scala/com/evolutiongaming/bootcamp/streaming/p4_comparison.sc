import cats.effect.{ContextShift, IO, Timer}
import fs2.Stream
import monix.reactive.{Observable, OverflowStrategy}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

implicit val timer: Timer[IO]     = IO.timer(ExecutionContext.parasitic)
implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

// Comparison

val stream: Stream[IO, Int] = Stream.range(1, 20).zipLeft(Stream.fixedRate[IO](100.millis))
val obs: Observable[Long]   = Observable.range(1, 20).throttle(100.millis, 1)

// For linear pipelines, API for both fs2 and monix are mostly the same

// Things easy in fs2:

// fs2.text, e.g. decoding byte streams

// sttp (http client) can stream results in either fs2 or monix
// For fs2, result is essentially IO[Stream[IO, Byte]]
val inputStr =
  """foo bar baz
    |qux bar baz
    |foO Foo fOO""".stripMargin

val httpResultFs2: IO[Stream[IO, Byte]] = IO {
  Stream
    .iterable[IO, Byte](inputStr.getBytes)
    .chunkLimit(5)
    .flatMap(Stream.chunk)
}

// This can easily be processed per-line
Stream
  .eval(httpResultFs2)
  .flatten
  .through(fs2.text.utf8Decode)
  .through(fs2.text.lines)
  .compile
  .toList
  .unsafeRunSync()

// Along with a lot of utilities for decoding, compression, etc.
fs2.text.base64.encode[IO]
fs2.hash.sha256[IO]
fs2.compression.gzip[IO]()

// Things easy in monix
// Operations involving time

// Take elements for 1 second
obs.takeByTimespan(1.second)
// Drop elements for 1 second
obs.dropByTimespan(1.second)

// Suppress elements arriving too fast
obs.debounce(100.millis)
// fs2 has that too
stream.debounce(100.millis)

// And variants of those
obs.debounceRepeated(100.millis) // debounce but keep emitting last item
obs.echoRepeated(100.millis) // If source emits nothing for 100 millis, emit last item
obs.sample(100.millis) // One element per 100 millis

obs.throttle(150.millis, 2) // Limit throughput to 2 items per 150 millis, backpressure if too fast
// there is equivalent for that in fs2
stream.metered(75.millis) // 1 item per 75 millis ~= 2 per 150 millis

// Buffering
// fs2: groupWithin
// Collect items, emit when collected 5 items, or when 100 millis passes
stream.groupWithin(5, 300.millis)

// monix
// Same operation
obs.bufferTimedAndCounted(300.millis, 5)
// And a lot of variants
obs.bufferTimed(300.millis)
obs.bufferTumbling(5)
obs.bufferSliding(5, 3)
// Including buffering by elements from other Observable
obs.bufferWithSelector(Observable.intervalWithFixedDelay(300.millis))

obs.asyncBoundary(OverflowStrategy.BackPressure(5))
obs.asyncBoundary(OverflowStrategy.Fail(5))
obs.asyncBoundary(OverflowStrategy.DropOld(5))

// Some operators for handling slow downstream
obs.whileBusyDropEvents

// Aggregate events into batches while downstream is busy, emit batches at once
obs.whileBusyAggregateEvents(event => Vector(event)) { case (agg, event) =>
  agg :+ event
}
