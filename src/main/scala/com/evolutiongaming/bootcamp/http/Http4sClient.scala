package com.evolutiongaming.bootcamp.http

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.client._
import org.http4s.client.blaze._
import org.http4s.implicits._
import scala.concurrent.ExecutionContext.global

object Http4sClient extends IOApp {
  val uri = uri"http://localhost:9000/hello"

  def putStrLn(s: String): IO[Unit] = IO.delay(println(s))

  def run(args: List[String]): IO[ExitCode] =
    BlazeClientBuilder[IO](global).resource.use { client =>
      for {
        _ <- client.expect[String](uri / "world") >>= putStrLn
        _ <- client.expect[String](Request[IO](Method.POST, uri).withEntity("world")) >>= putStrLn
        // Query parameters
        _ <- client.expect[String](uri / "int" / "42") >>= putStrLn
        _ <- client.expect[String]((uri / "int").withQueryParam("val", 42)) >>= putStrLn
      } yield ()
    }.as(ExitCode.Success)
}
