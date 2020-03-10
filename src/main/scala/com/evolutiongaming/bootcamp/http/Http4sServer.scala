package com.evolutiongaming.bootcamp.http

import cats.data.EitherT
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import fs2.Pipe
import fs2.concurrent.Queue
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame

object Http4sServer extends IOApp {
  val helloRoutes = HttpRoutes.of[IO] {
    // curl 'localhost:9000/hello/world'
    case GET -> Root / "hello" / name =>
      Ok(s"Hello $name!")

    // curl -XPOST 'localhost:9000/hello' -d 'world'
    case req @ POST -> Root / "hello" =>
      Ok(req.as[String].map(name => s"Hello $name!"))
  }

  // Query parameters

  val paramsRoutes = {
    final case class Number(n: Int)

    implicit val numberQueryParamDecoder: QueryParamDecoder[Number] =
      QueryParamDecoder[Int].map(Number)

    object NumberMatcher extends QueryParamDecoderMatcher[Number]("val")

    HttpRoutes.of[IO] {
      // curl 'localhost:9000/int/42'
      case GET -> Root / "int" / IntVar(n) =>
        Ok(s"Passed: $n")

      // curl 'localhost:9000/int?val=42'
      case GET -> Root / "int" :? NumberMatcher(number) =>
        Ok(s"Passed: ${number.n}")
    }
  }

  // Headers/cookies

  val headersRoutes = HttpRoutes.of[IO] {
    // curl -v 'localhost:9000/headers' -H 'Request-Header: request value'
    case req @ GET -> Root / "headers" =>
      val headers = req.headers.toList.map(h => h.name.value + ": " + h.value).mkString("\n")
      Ok(headers, Header("Custom-Header", "custom value"))

    // curl 'localhost:9000/cookies' -b "request-cookie=request_value"
    case req @ GET -> Root / "cookies" =>
      val cookies = req.cookies.map(c => c.name + ": " + c.content).mkString("\n")
      Ok(cookies).map(_.addCookie(ResponseCookie("custom-cookie", "custom value")))
  }

  // Body encoding/decoding

  val entityRoutes = {
    final case class Hello(name: String)

    implicit val helloDecoder = EntityDecoder.decodeBy(MediaType.text.plain) { m: Media[IO] =>
      val NameRegex = """\((.*)\)""".r
      EitherT {
        m.as[String].map {
          case NameRegex(s) => Hello(s).asRight
          case s => InvalidMessageBodyFailure(s"Invalid value: $s").asLeft
        }
      }
    }

    HttpRoutes.of[IO] {
      // curl -XPOST 'localhost:9000/entity' -d '(world)'
      case req @ POST -> Root / "entity" =>
        req.as[Hello].flatMap(hello => Ok(s"Hello ${hello.name}!"))
    }
  }

  // Body json encoding/decoding

  val jsonRoutes = {
    final case class Hello(name: String)

    import io.circe.generic.auto._
    import org.http4s.circe.CirceEntityCodec._

    // implicit val helloDecoder = jsonOf[IO, Hello]

    HttpRoutes.of[IO] {
      // curl -XPOST 'localhost:9000/json' -d '{"name": "world"}'
      case req @ POST -> Root / "json" =>
        req.as[Hello].flatMap(hello => Ok(s"Hello ${hello.name}!"))
    }
  }

  // WebSockets

  val wsEchoRoutes = HttpRoutes.of[IO] {
    // websocat 'ws://127.0.0.1:9000/wsecho'
    case GET -> Root / "wsecho" =>
      val textEcho: Pipe[IO, WebSocketFrame, WebSocketFrame] =
        _.collect {
          case WebSocketFrame.Ping(data) => WebSocketFrame.Pong(data)
          case WebSocketFrame.Text(msg, _) => WebSocketFrame.Text(msg)
        }

      Queue
        .unbounded[IO, WebSocketFrame]
        .flatMap { q =>
          WebSocketBuilder[IO].build(
            q.dequeue.through(textEcho),
            q.enqueue
          )
        }
  }

  val routes = helloRoutes <+> paramsRoutes <+> headersRoutes <+> entityRoutes <+> jsonRoutes <+> wsEchoRoutes

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(9000, "localhost")
      .withHttpApp(routes.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
