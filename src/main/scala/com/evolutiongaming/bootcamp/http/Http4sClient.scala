package com.evolutiongaming.bootcamp.http

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import scala.concurrent.ExecutionContext.global

object Http4sClient extends IOApp {
  val uri = uri"http://localhost:9000"

  def putStrLn(s: String): IO[Unit] = IO.delay(println(s))

  def run(args: List[String]): IO[ExitCode] =
    BlazeClientBuilder[IO](global).resource.use { client =>
      val dash = putStrLn("-" * 25)
      for {
        _ <- client.expect[String](uri / "hello" / "world") >>= putStrLn
        _ <- dash
        _ <- client.expect[String](Request[IO](Method.POST, uri / "hello").withEntity("world")) >>= putStrLn
        _ <- dash

        // Query parameters

        _ <- client.expect[String](uri / "int" / "42") >>= putStrLn
        _ <- dash
        _ <- client.expect[String]((uri / "int").withQueryParam("val", 42)) >>= putStrLn
        _ <- dash

        // Headers/cookies

        _ <- client.expect[String](Request[IO](uri = uri / "headers", headers = Headers.of(Header("Request-Header", "request value")))) >>= putStrLn
        _ <- dash
        _ <- client.expect[String](Request[IO](uri = uri / "cookies").addCookie(RequestCookie("request-cookie", "request_value"))) >>= putStrLn
        _ <- dash

        // Body encoding/decoding

        _ <- {
          final case class Hello(name: String)

          implicit val helloEncoder = EntityEncoder.stringEncoder[IO].contramap { hello: Hello =>
            s"(${hello.name})"
          }

          client.expect[String](Request[IO](Method.POST, uri / "entity").withEntity(Hello("world"))) >>= putStrLn
        }
        _ <- dash

        // Body json encoding/decoding

        _ <- {
          final case class Hello(name: String)

          import io.circe.generic.auto._
          import org.http4s.circe.CirceEntityCodec._

          // implicit val helloEncoder = jsonEncoderOf[IO, Hello]

          client.expect[String](Request[IO](Method.POST, uri / "json").withEntity(Hello("world"))) >>= putStrLn
        }
        _ <- dash
      } yield ()
    }.as(ExitCode.Success)
}
