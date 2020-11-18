package com.evolution.bootcamp.containers.webserver

import cats.effect.concurrent.Ref
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.staticcontent.{FileService, fileService}

import scala.concurrent.ExecutionContext.global

object Starter extends IOApp {

  def routes(alive: Ref[IO, Boolean]) = HttpRoutes.of[IO] {
    case GET -> Root / "alive" => alive.get.flatMap(Either.cond(_, Ok("Alive"), InternalServerError("Dead")).merge)
    case GET -> Root / "env" => Ok(scala.sys.env.mkString("\n"))
    case POST -> Root / "kill" => alive.set(false) *> Ok("Press F to Pay Respects")
    case GET -> Root => Ok("Ok")
  }

  def run(args: List[String]): IO[ExitCode] = {
    (for {
      _ <- IO(println("Example web server"))
      _ <- Blocker[IO].use { b =>
        for {
          aliveRef <- Ref.of[IO, Boolean](true)
          _ <- BlazeServerBuilder[IO](global)
            .bindHttp(10000, "0.0.0.0")
            .withHttpApp(Router(
              "/" -> routes(aliveRef),
              "files" -> fileService(FileService.Config[IO]("/tmp/files", b))
            ).orNotFound)
            .serve.compile.drain
        } yield ()
      }
    } yield ()).as(ExitCode.Success)
  }
}
