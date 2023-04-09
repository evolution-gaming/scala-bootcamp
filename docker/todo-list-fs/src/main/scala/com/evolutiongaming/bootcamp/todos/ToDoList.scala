package com.evolutiongaming.bootcamp.todos

import cats.effect.{ExitCode, IO, IOApp, Ref}
import fs2.concurrent.SignallingRef
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe.CirceEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{HttpApp, HttpRoutes}

object ToDoList extends IOApp with Http4sDsl[IO] with CirceEntityEncoder {

  def toDoRoutes(todos: ToDos[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root        =>
      todos.listAll.flatMap(Ok(_))
    case req @ POST -> Root =>
      for {
        text <- req.bodyText.compile.string
        todo <- todos.create(text)
        res  <- Created(todo)
      } yield res
  }

  def killRoutes(switch: KillSwitch): HttpRoutes[IO] =
    HttpRoutes.of[IO] { case POST -> Root =>
      switch.flip *> Ok("Killed")
    }

  def app(todos: ToDos[IO], killSwitch: KillSwitch): HttpApp[IO] =
    Router(
      "/todos" -> toDoRoutes(todos),
      "/kill"  -> killRoutes(killSwitch),
    ).orNotFound

  override def run(args: List[String]): IO[ExitCode] =
    KillSwitch.of.flatMap { killSwitch =>
      BlazeServerBuilder[IO]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(app(ToDos.of[IO], killSwitch))
        .serveWhile(killSwitch.signal, killSwitch.exitCode)
        .compile
        .lastOrError
    }

  final case class KillSwitch(signal: SignallingRef[IO, Boolean], exitCode: Ref[IO, ExitCode]) {
    val flip: IO[Unit] = exitCode.set(ExitCode.Error) *> signal.set(true)
  }

  object KillSwitch {
    def of: IO[KillSwitch] =
      for {
        signal   <- SignallingRef[IO, Boolean](false)
        exitCode <- Ref.of[IO, ExitCode](ExitCode.Success)
      } yield KillSwitch(signal, exitCode)
  }
}
