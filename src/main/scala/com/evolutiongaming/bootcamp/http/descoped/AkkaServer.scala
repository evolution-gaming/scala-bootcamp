package com.evolutiongaming.bootcamp.http.descoped

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{HttpCookie, RawHeader}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.scaladsl.{Flow, Sink}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object AkkaServer extends App {
  implicit val system = ActorSystem("my-system")
  implicit val executionContext = system.dispatcher

  val helloRoute =
    path("hello" / Segment) { name =>
      get {
        // curl 'localhost:9000/hello/world'
        complete(s"Hello $name!")
      }
    } ~ path("hello") {
      post {
        entity(as[String]) { name =>
          // curl -XPOST 'localhost:9000/hello' -d 'world'
          complete(s"Hello $name!")
        }
      }
    }

  // Query parameters

  val paramsRoute =
    (get & pathPrefix("int")) {
      path(IntNumber) { n =>
        // curl 'localhost:9000/int/42'
        complete(s"Passed: $n")
      } ~ parameter(Symbol("val")) { n =>
        // curl 'localhost:9000/int?val=42'
        complete(s"Passed: $n")
      }
    }

  // Headers/cookies

  val headersRoute = get {
    path("headers") {
      headerValuePF {
        case HttpHeader("request-header", v) => v
      } { header =>
        respondWithHeader(RawHeader("Response-Header", "response value")) {
          // curl -v 'localhost:9000/headers' -H 'Request-Header: request value'
          complete(header)
          }
      }
    } ~ path("cookies") {
      cookie("request-cookie") { cookie =>
        setCookie(HttpCookie("response-cookie", value = "response_value")) {
          // curl 'localhost:9000/cookies' -b "request-cookie=request_value"
          complete(cookie.value)
        }
      }
    }
  }

  // Body encoding/decoding

  val entityRoute = {
    final case class Hello(name: String)

    val NameRegex = """\((.*)\)""".r
    implicit val helloUnmarshaller = Unmarshaller.stringUnmarshaller.map {
      case NameRegex(s) => Hello(s)
      case s => throw new IllegalArgumentException(s"Invalid value: $s")
    }

    (post & path("entity")) {
      post {
        entity(as[Hello]) { hello =>
          // curl -XPOST 'localhost:9000/hello' -d 'world'
          complete(s"Hello ${hello.name}!")
        }
      }
    }
  }

  // Body json encoding/decoding

  val jsonRoute = {
    final case class Hello(name: String)

    import io.circe.generic.auto._
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

    // implicit val helloUnmarshaller = FailFastCirceSupport.unmarshaller[Hello]

    (post & path("json")) {
      entity(as[Hello]) { hello =>
        // curl -XPOST 'localhost:9000/json' -d '{"name": "world"}' -H "Content-Type: application/json"
        complete(s"Hello ${hello.name}!")
      }
    }
  }

  // WebSockets

  val wsEchoRoute = path("wsecho") {
    val flow = Flow[Message].collect {
      case TextMessage.Strict(msg) => TextMessage.Strict(msg)
    }

    // websocat 'ws://127.0.0.1:9000/wsecho'
    handleWebSocketMessages(flow)
  }

  // Multipart

  val multipartRoute = (post & path("multipart")) {
    entity(as[Multipart.FormData]) { m =>
      // curl -XPOST 'localhost:9000/multipart' -F 'text=request value' -F file=@file.txt
      complete(m.parts.map(_.name).runWith(Sink.seq).map(_.mkString("\n")))
    }
  }

  val route = helloRoute ~ paramsRoute ~ headersRoute ~ entityRoute ~ jsonRoute ~ multipartRoute ~ wsEchoRoute

  val binding = Http().bindAndHandle(route, "localhost", 9000)

  sys.addShutdownHook {
    binding.flatMap(_.terminate(hardDeadline = 3.seconds)).flatMap { _ =>
      system.terminate()
    }
  }
}
