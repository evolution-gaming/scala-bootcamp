package com.evolutiongaming.bootcamp.streaming.examples

import cats.effect.{IO, IOApp}

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.util.Random
import fs2.{Chunk, Stream}

import scala.concurrent.duration.DurationInt

object PollDatabase extends IOApp.Simple {
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

  def run: IO[Unit] = Stream
    .unfoldChunkEval(Instant.now().minusSeconds(10)) { after =>
      getItemsAfter(after).flatMap { rows =>
        if (rows.nonEmpty) { // We have data, return it along with new `after`
          val lastAdded = rows.map(_.lastAdded).max
          IO.pure(Option(Chunk.iterable(rows) -> lastAdded))
        } else { // No data, wait a bit and repeat
          IO.sleep(100.millis) *> IO.pure(Some(Chunk.empty -> after))
        }
      }
    }
    .take(15)
    .debugChunks()
    .compile
    .drain // Note that first 10 elements come in two chunks
}
