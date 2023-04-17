package com.evolutiongaming.bootcamp.http

import cats.effect.std.Queue
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.concurrent.Topic
import fs2.{Pipe, Stream}
import org.http4s.ember.server._
import org.http4s.client.websocket.{WSFrame, WSRequest}
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.jdkhttpclient.JdkWSClient
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.http4s.{HttpRoutes, _}

import java.net.http.HttpClient

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

  private def echoRoute(wsb: WebSocketBuilder2[IO]) = HttpRoutes.of[IO] {

    // websocat "ws://localhost:9002/echo"
    case GET -> Root / "echo" =>
      // Pipe is a stream transformation function of type `Stream[F, I] => Stream[F, O]`. In this case
      // `I == O == WebSocketFrame`. So the pipe transforms incoming WebSocket messages from the client to
      // outgoing WebSocket messages to send to the client.
      val echoPipe: Pipe[IO, WebSocketFrame, WebSocketFrame] =
        _.collect { case WebSocketFrame.Text(message, _) =>
          WebSocketFrame.Text(message)
        }

      for {
        // Unbounded queue to store WebSocket messages from the client, which are pending to be processed.
        // For production use bounded queue seems a better choice. Unbounded queue may result in out of
        // memory error, if the client is sending messages quicker than the server can process them.
        queue    <- Queue.unbounded[IO, WebSocketFrame]
        response <- wsb.build(
          // Sink, where the incoming WebSocket messages from the client are pushed to.
          receive = _.evalMap(queue.offer),
          // Outgoing stream of WebSocket messages to send to the client.
          send = Stream.repeatEval(queue.take).through(echoPipe),
        )
      } yield response

    // Exercise 1. Change the echo route to respond with the current time, when the client sends "time". Allow
    // whitespace characters before and after the command, so " time " should also be considered valid. Note
    // that getting the current time is a side effect.

    // Exercise 2. Change the echo route to notify the client every 5 seconds how long it is connected.
    // Tip: you can merge streams via `merge` operator.
  }

  // Topics provide an implementation of the publish-subscribe pattern with an arbitrary number of
  // publishers and an arbitrary number of subscribers.
  private def chatRoute(chatTopic: Topic[IO, String])(wsb: WebSocketBuilder2[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {

    // websocat "ws://localhost:9002/chat"
    case GET -> Root / "chat" =>
      wsb.build(
        // Sink, where the incoming WebSocket messages from the client are pushed to.
        receive = chatTopic.publish.compose[Stream[IO, WebSocketFrame]](_.collect {
          case WebSocketFrame.Text(message, _) => message
        }),
        // Outgoing stream of WebSocket messages to send to the client.
        send = chatTopic.subscribe(maxQueued = 10).map(WebSocketFrame.Text(_)),
      )

    // Exercise 3. Change the chat route to use the first message from a client as its username and prepend
    // it to every follow-up message. Tip: you will likely need to use fs2.Pull.
  }

  private def httpApp(chatTopic: Topic[IO, String])(wsb: WebSocketBuilder2[IO]): HttpApp[IO] = {
    echoRoute(wsb) <+> chatRoute(chatTopic)(wsb)
  }.orNotFound

  override def run(args: List[String]): IO[ExitCode] =
    for {
      chatTopic <- Topic[IO, String]
      _         <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"127.0.0.1")
        .withPort(port"9002")
        .withHttpWebSocketApp(httpApp(chatTopic))
        .build
        .useForever
    } yield ExitCode.Success
}

// Http4s provides a purely functional wrapper for the built-in JDK 11+ HTTP client.
object WebSocketClient extends IOApp {

  private val uri = uri"ws://localhost:9002/echo"

  private def printLine(string: String = ""): IO[Unit] = IO(println(string))

  override def run(args: List[String]): IO[ExitCode] = {
    val clientResource = Resource
      .eval(IO(HttpClient.newHttpClient()))
      .flatMap(JdkWSClient[IO](_).connectHighLevel(WSRequest(uri)))

    clientResource.use { client =>
      for {
        _ <- client.send(WSFrame.Text("Hello, world!"))
        _ <- client.receiveStream
          .collectFirst { case WSFrame.Text(s, _) =>
            s
          }
          .compile
          .string >>= printLine
      } yield ExitCode.Success
    }
  }
}

// Attributions and useful links:
// https://en.wikipedia.org/wiki/WebSocket
// https://javascript.info/websocket
// https://hpbn.co/websocket/
