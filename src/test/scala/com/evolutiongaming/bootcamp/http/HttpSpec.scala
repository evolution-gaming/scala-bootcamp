package com.evolutiongaming.bootcamp.http

import cats.effect.IO
import cats.syntax.option._
import com.evolutiongaming.bootcamp.http.HttpServer.httpApp
import fs2.Stream
import org.http4s._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.multipart.{Multipart, Part}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class HttpSpec
  extends AnyFreeSpec
    with Matchers {

  "HttpService should" - {

    "validate timestamp" in {
      val validResponseIO = httpApp.run(
        Request(method = Method.GET, uri = uri"/params/validate"
          .withQueryParam(key = "timestamp", value = "2020-11-04T14:19:54.736Z"))
      )
      check[String](
        actualResponseIO = validResponseIO,
        expectedStatus = Status.Ok,
        expectedBody = "Timestamp is valid".some,
      )

      val invalidResponseIO = httpApp.run(
        Request(method = Method.GET, uri = uri"/params/validate"
          .withQueryParam(key = "timestamp", value = "2020-40-04T14:19:54.736Z"))
      )
      check[String](
        actualResponseIO = invalidResponseIO,
        expectedStatus = Status.BadRequest,
        expectedBody = "Timestamp is invalid".some,
      )
    }

    "increment counter cookie" in {
      val incrementedCounterResponseIO = httpApp.run(
        Request(method = Method.GET, uri = uri"/cookies").addCookie("counter", "9")
      )
      check[String](
        actualResponseIO = incrementedCounterResponseIO,
        expectedStatus = Status.Ok,
        expectedBody = None,
        expectedResponseCookie = ResponseCookie("counter", "10").some,
      )

      val newCounterResponseIO = httpApp.run(
        Request(method = Method.GET, uri = uri"/cookies")
      )
      check[String](
        actualResponseIO = newCounterResponseIO,
        expectedStatus = Status.Ok,
        expectedBody = None,
        expectedResponseCookie = ResponseCookie("counter", "1").some,
      )
    }

    "count character occurrences" in {

      def multipartRequestWith(characterValue: String): Request[IO] = {
        val multipart = Multipart[IO](Vector(
          Part.formData(name = "character", value = characterValue),
          Part.fileData(
            name = "file",
            filename = "test.txt",
            entityBody = Stream.emits(os = "Some test text here...".map(_.toByte)),
            headers = `Content-Type`(MediaType.text.plain),
          ),
        ))
        val multipartBody = EntityEncoder[IO, Multipart[IO]].toEntity(multipart).body
        Request(
          method = Method.POST,
          uri = uri"/multipart",
          headers = multipart.headers,
          body = multipartBody,
        )
      }

      val validResponseIO = httpApp.run {
        multipartRequestWith(characterValue = "e")
      }
      check[String](
        actualResponseIO = validResponseIO,
        expectedStatus = Status.Ok,
        expectedBody = "5".some,
      )

      val invalidResponseIO = httpApp.run {
        multipartRequestWith(characterValue = "abc")
      }
      check[String](
        actualResponseIO = invalidResponseIO,
        expectedStatus = Status.BadRequest,
        expectedBody = None,
      )
    }
  }

  private def check[A](
    actualResponseIO: IO[Response[IO]],
    expectedStatus: Status,
    expectedBody: Option[A],
    expectedResponseCookie: Option[ResponseCookie] = None,
  )(implicit
    decoder: EntityDecoder[IO, A],
  ): Unit = (for {
    actualResponse <- actualResponseIO
    _ <- IO(actualResponse.status shouldBe expectedStatus)
    _ <- expectedBody match {
      case None       => actualResponse.body.compile.toVector.map(_ shouldBe empty)
      case Some(body) => actualResponse.as[A].map(_ shouldBe body)
    }
    _ <- expectedResponseCookie match {
      case None                 => IO.unit
      case Some(responseCookie) => IO(actualResponse.cookies should contain(responseCookie))
    }
  } yield ()).unsafeRunSync()
}
