package com.evolutiongaming.bootcamp.http

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Pipe
import fs2.concurrent.Queue
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame

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

// Homework. Place the solution under `http` package in your homework repository.
//
// Write a server and a client that play a number guessing game together.
//
// Communication flow should be as follows:
// 1. The client asks the server to start a new game by providing the minimum and the maximum number that can
//    be guessed, as well as the maximum number of attempts.
// 2. The server comes up with some random number within the provided range.
// 3. The client starts guessing the number. Upon each attempt, the server evaluates the guess and responds to
//    the client, whether the current number is lower, greater or equal to the guessed one.
// 4. The game ends when the number is guessed or there are no more attempts left. At this point the client
//    should terminate, while the server may continue running forever.
// 5. The server should support playing many separate games (with different clients) at the same time.
//
// Use HTTP or WebSocket for communication. The exact protocol and message format to use is not specified and
// should be designed while working on the task.
object GuessServer {
  // ...
}
object GuessClient {
  // ...
}

// Attributions and useful links:
// https://en.wikipedia.org/wiki/WebSocket
// https://javascript.info/websocket
// https://hpbn.co/websocket/
