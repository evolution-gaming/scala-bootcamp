package com.evolutiongaming.bootcamp.http

import java.time.{Instant, LocalDate}

import cats.data.{EitherT, Validated}
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.syntax.all._
import com.evolutiongaming.bootcamp.http.Protocol._
import org.http4s._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.multipart.{Multipart, Part}
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext
import scala.util.Try

object HttpIntroduction {

  // HTTP

  // HTTP (HyperText Transfer Protocol) is the foundation of data exchange on the web. It is an application
  // layer client-server protocol. The client (i.e. a web browser or some other application) initiates
  // requests and receives responses back from the server.

  // HTTP evolves over time. The most widely used versions as of now are HTTP/1.1 and HTTP/2. Both versions
  // use TCP (Transmission Control Protocol) as their transport. HTTP/3, currently in development, will switch
  // to using QUIC instead. We will not cover differences between HTTP versions in this lecture.
  // If still interested, see:
  // https://blog.cloudflare.com/http3-the-past-present-and-future/

  // HTTP is not secure by default. To make HTTP connections secure, one has to use HTTPS, an extension of
  // HTTP protocol that uses TLS (Transport Layer Security) for encryption and authentication purposes. Again,
  // this will not be covered in this lecture. If still interested, see:
  // https://www.ssl.com/faqs/what-is-https/

  // HTTP OVERVIEW

  // 1. HTTP is simple. HTTP messages can be read and understood by humans.

  // Sample HTTP request:
  //
  // GET / HTTP/1.1
  // Host: www.google.com
  // Accept-Language: en

  // Sample HTTP response:
  //
  // HTTP/1.1 200 OK
  // Date: Sat, 09 Oct 2010 14:28:02 GMT
  // Content-Length: 29769
  // Content-Type: text/html
  //
  // <!DOCTYPE html... (here comes the 29769 bytes of the requested web page)

  // HTTP request methods:
  //
  // GET
  // The GET method requests a representation of the specified
  // resource. Requests using GET should only retrieve data and
  // should have no other effect.
  //
  // POST
  // The POST request method requests that a web server accepts the
  // data enclosed in the body of the request message, most likely
  // for storing it.
  //
  // PUT
  // The PUT method requests that the enclosed entity be stored under
  // the supplied URI.
  //
  // DELETE
  // The DELETE method deletes the specified resource.
  //
  // PATCH
  // The PATCH method applies partial modifications to a resource.

  // | HTTP method | Request has Body | Safe | Idempotent | Cacheable |
  // |-------------+------------------+------+------------+-----------|
  // | GET         | Optional         | Yes  | Yes        | Yes       |
  // | POST        | Yes              | No   | No         | Yes       |
  // | PUT         | Yes              | No   | Yes        | No        |
  // | DELETE      | Optional         | No   | Yes        | No        |
  // | PATCH       | Yes              | No   | No         | No        |

  // HTTP response status codes:
  //
  // 1xx Informational
  // 2xx Successful
  // 3xx Redirection
  // 4xx Client Error
  // 5xx Server Error

  // 2. HTTP is easily extensible. For example, via custom HTTP headers.

  // 3. HTTP is stateless, but not sessionless. Each HTTP request is independent, it has no links to other
  // requests sent over the same connection. However, HTTP supports cookies (small pieces of data stored by
  // the client and passed alongside related HTTP requests), which allow the use of stateful sessions.

  // 4. HTTP resources are identified and located on the network via URLs (Uniform Resource Locators):
  //
  //             userinfo         host        port
  //        ┌───────┴───────┐ ┌────┴────────┐ ┌┴┐
  // http://john.doe:password@www.example.com:123/forum/questions/?tag=networking&order=newest#top
  // └─┬───┘└──────────┬────────────────────────┘└─┬─────────────┘└────────┬─────────────────┘└┬─┘
  // scheme        authority                      path                   query              fragment
}

// Models that are shared between `HttpServer` and `HttpClient` below.
object Protocol {
  final case class User(name: String, age: Int)
  final case class Greeting(text: String, timestamp: Instant)
}

object HttpServer extends IOApp {

  // Http4s is a type safe, purely functional, streaming HTTP library for Scala.
  // Let's build an HTTP server using this library powered by Cats Effect IO.

  // SIMPLE GET AND POST REQUESTS

  private val helloRoutes = HttpRoutes.of[IO] {

    // curl "localhost:9001/hello/world"
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name!")

    // curl -XPOST "localhost:9001/hello" -d "world"
    case req @ POST -> Root / "hello" =>
      Ok(req.as[String].map(name => s"Hello again, $name!"))
  }

  // PATH AND QUERY PARAMETERS

  private val paramsRoutes = {

    // URL path parameters are strings by default. But they can be extracted and converted to any specific
    // type via a custom extractor object. By convention the extractor of a value of type `T` must implement
    // the following method: `def unapply(value: String): Option[T]`.
    //
    // Http4s provides a few predefined extractor objects, others must be supplied manually:
    // import org.http4s.dsl.io.{IntVar, LongVar, UUIDVar}
    object LocalDateVar {
      def unapply(value: String): Option[LocalDate] =
        Try(LocalDate.parse(value)).toOption
    }

    // URL query parameters must have corresponding `QueryParamDecoderMatcher` objects to extract them.
    // `QueryParamDecoderMatcher[T]` in turn needs an `implicit QueryParamDecoder[T]` in scope that implements
    // the actual extraction logic. Http4s contains a number of predefined `QueryParamDecoder` instances for
    // simple types, others must be supplied manually.
    implicit val localDateDecoder: QueryParamDecoder[LocalDate] = { param =>
      Validated
        .catchNonFatal(LocalDate.parse(param.value))
        .leftMap(t => ParseFailure(s"Failed to decode LocalDate", t.getMessage))
        .toValidatedNel
    }
    object LocalDateMatcher extends QueryParamDecoderMatcher[LocalDate](name = "date")

    HttpRoutes.of[IO] {

      // curl "localhost:9001/params/2020-11-10"
      case GET -> Root / "params" / LocalDateVar(localDate) =>
        Ok(s"Matched path param: $localDate")

      // curl "localhost:9001/params?date=2020-11-10"
      case GET -> Root / "params" :? LocalDateMatcher(localDate) =>
        Ok(s"Matched query param: $localDate")

      // Exercise 1. Implement HTTP endpoint that validates the provided timestamp in ISO-8601 format. If valid,
      // 200 OK status must be returned with "Timestamp is valid" string in the body. If not valid,
      // 400 Bad Request status must be returned with "Timestamp is invalid" string in the body.
      // curl "localhost:9001/params/validate?timestamp=2020-11-04T14:19:54.736Z"
    }
  }

  // HEADERS AND COOKIES

  // There are limitations to what characters can be used in HTTP headers and cookies. The exact limitations
  // are tricky, we will not cover them in detail. The safest options are:
  // * in headers to use US-ASCII characters only;
  // * in cookies to use US-ASCII characters only, excluding whitespace, double quote, comma, semicolon and backslash.
  private val headersRoutes = HttpRoutes.of[IO] {

    // curl "localhost:9001/headers" -H "Request-Header: Request header value"
    case req @ GET -> Root / "headers" =>
      Ok(s"Received headers: ${ req.headers }", Header("Response-Header", "Response header value"))

    // Exercise 2. Implement HTTP endpoint that attempts to read the value of the cookie named "counter". If
    // present and contains an integer value, it should add 1 to the value and request the client to update
    // the cookie. Otherwise it should request the client to store "1" in the "counter" cookie.
    // curl -v "localhost:9001/cookies" -b "counter=9"
  }

  // JSON ENTITIES

  // Http4s provides integration with the following JSON libraries out of the box: Circe, Argonaut and Json4s.
  // With some boilerplate it is possible to integrate Http4s with any other library that does entity
  // serialization to and from JSON, XML, other formats.
  private val jsonRoutes = {
    import io.circe.generic.auto._
    import org.http4s.circe.CirceEntityCodec._

    // User JSON decoder can also be declared explicitly instead of importing from `CirceEntityCodec`:
    // implicit val userDecoder = org.http4s.circe.jsonOf[IO, User]

    HttpRoutes.of[IO] {

      // curl -XPOST "localhost:9001/json" -d '{"name": "John", "age": 18}' -H "Content-Type: application/json"
      case req @ POST -> Root / "json" =>
        req.as[User].flatMap { user =>
          val greeting = Greeting(text = s"Hello, ${ user.name }!", timestamp = Instant.now())
          Ok(greeting)
        }
    }
  }

  // BODY ENCODING/DECODING

  // It is possible to write custom decoders for the HTTP body.
  private val entityRoutes = {
    implicit val userDecoder = EntityDecoder.decodeBy(MediaType.text.plain) { m: Media[IO] =>
      val NameRegex = """\((.*),(\d{1,3})\)""".r
      EitherT {
        m.as[String].map {
          case NameRegex(name, age) => User(name, age.toInt).asRight
          case s => InvalidMessageBodyFailure(s"Invalid value: $s").asLeft
        }
      }
    }

    HttpRoutes.of[IO] {

      // curl -XPOST 'localhost:9001/entity' -d '(John,18)'
      case req @ POST -> Root / "entity" =>
        req.as[User].flatMap(user => Ok(s"Hello ${ user.name }!"))
    }
  }

  // MULTIPART REQUESTS

  // Multipart requests combine multiple sets of data. For example, they can be used to transfer text strings
  // and binary files simultaneously, which is often handy when submitting web forms.
  private val multipartRoutes = HttpRoutes.of[IO] {

    // Exercise 3. Implement HTTP endpoint that processes a multipart request. The request is expected to have
    // the following parts:
    // 1. character - contains a single character;
    // 2. file - contains a text file.
    //
    // The endpoint should count how many times the given letter is present in the file and return that number
    // back in OK 200 response. 400 Bad Request response with an empty body is expected if the request is
    // invalid for any reason.
    // curl -XPOST "localhost:9001/multipart" -F "character=n" -F file=@text.txt
    case req @ POST -> Root / "multipart" =>
      req.as[Multipart[IO]].flatMap { multipart =>
        ???
      }
  }

  private[http] val httpApp = {
    helloRoutes <+> paramsRoutes <+> headersRoutes <+> jsonRoutes <+> entityRoutes <+> multipartRoutes
  }.orNotFound

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(port = 9001, host = "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}

object HttpClient extends IOApp {

  // Now let's build an HTTP client using Http4s. It will call endpoints, exposed by the HTTP server above.

  private val uri = uri"http://localhost:9001"

  private def printLine(string: String = ""): IO[Unit] = IO(println(string))

  def run(args: List[String]): IO[ExitCode] =
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .parZip(Blocker[IO]).use { case (client, blocker) =>
      for {
        _ <- printLine(string = "Executing simple GET and POST requests:")
        _ <- client.expect[String](uri / "hello" / "world") >>= printLine
        _ <- client.expect[String](Method.POST("world", uri / "hello")) >>= printLine
        _ <- printLine()

        _ <- printLine(string = "Executing requests with path and query parameters:")
        _ <- client.expect[String](uri / "params" / "2020-11-10") >>= printLine
        _ <- client.expect[String]((uri / "params").withQueryParam(key = "date", value = "2020-11-10")) >>= printLine

        // Exercise 4. Call HTTP endpoint, implemented in scope of Exercise 1.
        // curl "localhost:9001/params/validate?timestamp=2020-11-04T14:19:54.736Z"
        _ <- printLine()

        _ <- printLine(string = "Executing request with headers and cookies:")
        _ <- client.expect[String](Method.GET(uri / "headers", Header("Request-Header", "Request header value"))) >>= printLine

        // Exercise 5. Call HTTP endpoint, implemented in scope of Exercise 2.
        // curl -v "localhost:9001/cookies" -b "counter=9"
        _ <- printLine()

        _ <- printLine(string = "Executing request with JSON entities:")
        _ <- {
          import io.circe.generic.auto._
          import org.http4s.circe.CirceEntityCodec._

          // User JSON encoder can also be declared explicitly instead of importing from `CirceEntityCodec`:
          // implicit val helloEncoder = org.http4s.circe.jsonEncoderOf[IO, Hello]

          client.expect[Greeting](Method.POST(User("John", 18), uri / "json"))
            .flatMap(greeting => printLine(greeting.toString))
        }
        _ <- printLine()

        _ <- printLine(string = "Executing request with custom encoded entities:")
        _ <- {
          implicit val Encoder = EntityEncoder.stringEncoder[IO].contramap { user: User =>
            s"(${user.name},${user.age})"
          }

          client.expect[String](Method.POST(User("John", 18), uri / "entity")) >>= printLine
        }
        _ <- printLine()

        _ <- printLine(string = "Executing multipart requests:")
        _ <- {
          val file = getClass.getResource("/text.txt")
          val multipart = Multipart[IO](Vector(
            Part.formData("character", "n"),
            Part.fileData("file", file, blocker, `Content-Type`(MediaType.text.plain))
          ))
          client.expect[String](Method.POST(multipart, uri / "multipart").map(_.withHeaders(multipart.headers))) >>= printLine
        }
        _ <- printLine()
      } yield ()
    }.as(ExitCode.Success)
}

// Attributions and useful links:
// https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol
// https://developer.mozilla.org/en-US/docs/Web/HTTP/Overview
// https://http4s.org/latest/dsl/
