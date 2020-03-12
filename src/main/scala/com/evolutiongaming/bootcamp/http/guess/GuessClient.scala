package com.evolutiongaming.bootcamp.http.guess

import cats.data.EitherT
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import scala.concurrent.ExecutionContext.global

object GuessClient extends IOApp {
  val uri = uri"http://localhost:9000"

  def putStrLn(s: String): IO[Unit] = IO.delay(println(s))

  sealed trait GuessResult
  case object Lower extends GuessResult
  case object Equal extends GuessResult
  case object Greater extends GuessResult

  implicit val guessResultDecoder = EntityDecoder.decodeBy(MediaType.text.plain) { m: Media[IO] =>
    EitherT {
      m.as[String].map {
        case "lower" => Lower.asRight
        case "equal" => Equal.asRight
        case "greater" => Greater.asRight
        case s => InvalidMessageBodyFailure(s"Invalid value: $s").asLeft[GuessResult]
      }
    }
  }

  def make(client: Client[IO], left: Int, right: Int): IO[String] =
    client.expect[String](Method.POST((uri / "guess").withQueryParams(Map("left" -> left, "right" -> right))))

  def guess(client: Client[IO], id: String, left: Int, right: Int): IO[Int] = {
    val middle = (right - left) / 2 + left
    val result = client.expect[GuessResult]((uri / "guess" / id).withQueryParam("number", middle))
    if (left == right) {
      result.flatMap {
        case Equal => left.pure[IO]
        case _ => IO.raiseError(new Throwable("Hey! You are cheating!"))
      }
    } else {
      result.flatMap {
        case Lower => guess(client, id, middle + 1, right)
        case Greater => guess(client, id, left, middle)
        case Equal => middle.pure[IO]
      }
    }
  }

  def run(args: List[String]): IO[ExitCode] =
    BlazeClientBuilder[IO](global).resource.use { client =>
      val left = 0
      val right = 1000
      make(client, left, right).flatMap(guess(client, _, left, right)).map(n => s"The result is: $n") >>= putStrLn
    }.as(ExitCode.Success)
}
