package com.evolutiongaming.bootcamp.http

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.syntax.all._
import fs2.Pipe
import fs2.concurrent.Queue
import org.http4s._
import org.http4s.client.jdkhttpclient.{JdkWSClient, WSConnectionHighLevel, WSFrame, WSRequest}
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame

import java.net.http.HttpClient
import scala.concurrent.ExecutionContext

object WebSocketIntroduction {

  // WEBSOCKET

  // One of the main limitations of HTTP is its request-response model. The server can only send data to the
  // client, when the client requests it to. Unlike HTTP, WebSocket provides full-duplex communication. That
  // means the client and the server can send data to each other in both directions at the same time.

  // WebSocket is distinct from HTTP. However, both protocols use TCP (Transmission Control Protocol) as their
  // transport. In addition, WebSocket utilizes the same ports as HTTP by default (443 and 80) and uses HTTP
  // `Upgrade` header during its handshake. It means that, to establish a WebSocket connection, the client and
  // the server establish an HTTP connection first. Then the client proposes an upgrade to WebSocket. If the
  // server accepts, a new WebSocket connection is established.

  // WebSocket communication consists of frames (data fragments), which can be sent both by the client and
  // the server. Frames can be of several types:
  // * text frames contain text data;
  // * binary frames contain binary data;
  // * ping/pong frames check the connection (when sent from the server, the client responds automatically);
  // * other service frames: connection close frame, etc.

  // Developers usually directly work with text and binary frames only. In contrary to HTTP, WebSocket does
  // not enforce any specific message format, so frames can contain any text or binary data.
}

object WebSocketServer extends IOApp {

  // Let's build a WebSocket server using Http4s.

  private val webSocketRoute = HttpRoutes.of[IO] {

    // websocat "ws://localhost:9002/echo"
    case GET -> Root / "echo" =>
      // Pipe is a stream transformation function of type `Stream[F, I] => Stream[F, O]`. In this case
      // `I == O == WebSocketFrame`. So the pipe transforms incoming WebSocket messages from the client to
      // outgoing WebSocket messages to send to the client.
      val echoPipe: Pipe[IO, WebSocketFrame, WebSocketFrame] =
        _.collect {
          case WebSocketFrame.Text(message, _) => WebSocketFrame.Text(message)
        }

      for {
        // Unbounded queue to store WebSocket messages from the client, which are pending to be processed.
        // For production use bounded queue seems a better choice. Unbounded queue may result in out of
        // memory error, if the client is sending messages quicker than the server can process them.
        queue <- Queue.unbounded[IO, WebSocketFrame]
        response <- WebSocketBuilder[IO].build(
          // Sink, where the incoming WebSocket messages from the client are pushed to.
          receive = queue.enqueue,
          // Outgoing stream of WebSocket messages to send to the client.
          send = queue.dequeue.through(echoPipe),
        )
      } yield response
  }

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(port = 9002, host = "localhost")
      .withHttpApp(webSocketRoute.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}

// Regrettably, Http4s does not yet provide a WebSocket client (contributions are welcome!):
// https://github.com/http4s/http4s/issues/330
// But there is an Http4s wrapper for builtin JDK HTTP client.
object WebSocketClient extends IOApp {
  private val uri = uri"ws://localhost:9002/echo"

  private def printLine(string: String = ""): IO[Unit] = IO(println(string))

  override def run(args: List[String]): IO[ExitCode] = {
    val clientResource = Resource.eval(IO(HttpClient.newHttpClient()))
      .flatMap(JdkWSClient[IO](_).connectHighLevel(WSRequest(uri)))

    clientResource.use { client =>
      for {
        _ <- client.send(WSFrame.Text("hello"))
        _ <- client.receiveStream.collectFirst {
          case WSFrame.Text(s, _) => s
        }.compile.string >>= printLine
      } yield ExitCode.Success
    }
  }
}

// Attributions and useful links:
// https://en.wikipedia.org/wiki/WebSocket
// https://javascript.info/websocket
// https://hpbn.co/websocket/
