package com.evolutiongaming.bootcamp.streaming.examples

import cats.effect.{IO, IOApp}
import fs2.{Chunk, Stream}

object ByteStreams extends IOApp.Simple {
  def run: IO[Unit] = {
    // Byte streams
    // fs2 can efficiently represent raw byte streams as Stream[F, Byte]
    val inputStr                        =
      """foo bar baz
        |qux bar baz
        |foO Foo fOO """.stripMargin
    val networkStream: Stream[IO, Byte] =
      Stream.iterable(inputStr.getBytes).chunkLimit(5).flatMap(Stream.chunk)

    for {
      _ <- IO.println("Actual contents, chunk boundaries are not the same as word/line boundaries:")
      _ <- networkStream.debugChunks().compile.drain
      _ <- IO.println("Can still be converted to words:")
      _ <- networkStream
        .through(fs2.text.utf8.decode)
        .debugChunks(formatter = c => s"text: $c")
        .through(fs2.text.lines)
        .debugChunks(formatter = c => s"line: $c")
        .mapChunks(_.flatMap(line => Chunk.array(line.split("\\s+"))))
        .debugChunks(formatter = c => s"word: $c")
        .compile
        .toList
    } yield ()
  }
}
