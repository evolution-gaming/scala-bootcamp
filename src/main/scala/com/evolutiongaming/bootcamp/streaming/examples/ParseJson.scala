package com.evolutiongaming.bootcamp.streaming.examples

import cats.effect.{IO, IOApp}
import cats.syntax.either._
import fs2.Stream
import io.circe.Json

object ParseJson extends IOApp.Simple {
  // Parsing json-lines and other structured streams
  val stream = Stream(
    """|{"foo": 42}
       |[1, 2, 3]
       |{"more":"json"}
       |""".stripMargin
  ).covary[IO]
    .through(fs2.text.lines)
    .filter(_.nonEmpty)
    .evalMapChunk(line => io.circe.parser.parse(line).liftTo[IO])

  // When decoding, consider dropping unparseable elements
  // Otherwise: decoding fails -> stream fails -> entire app crashes
  def safeDecode(line: String): IO[Option[Json]] = {
    io.circe.parser.decode[Json](line) match {
      case Right(json)           => IO.pure(Some(json))
      case Left(decodingFailure) =>
        for {
          // Scream into logs/metrics here
          _ <- IO.println(s"Decoding failed for $line, error: ${decodingFailure.getMessage}")
        } yield None
    }
  }

  val otherStream = Stream(
    """|{"json": "ok"}
       |{"json": "ok"}
       |this is not json
       |{"other": "json"}
       |""".stripMargin
  ).covary[IO]
    .through(fs2.text.lines)
    .evalMapChunk(safeDecode)
    .unNone // unwrap Option

  def run: IO[Unit] = for {
    jsons <- otherStream.compile.toList
    _     <- IO.println(jsons.map(json => json.noSpaces))
  } yield ()
}
