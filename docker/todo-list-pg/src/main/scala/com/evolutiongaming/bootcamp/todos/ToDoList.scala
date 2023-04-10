package com.evolutiongaming.bootcamp.todos

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.Host
import org.http4s.circe.CirceEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.{HttpApp, HttpRoutes}

object ToDoList extends IOApp.Simple with Http4sDsl[IO] with CirceEntityEncoder {

  def routes(todos: ToDos[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root        =>
      todos.listAll.flatMap(Ok(_))
    case req @ POST -> Root =>
      for {
        text <- req.bodyText.compile.string
        todo <- todos.create(text)
        res  <- Created(todo)
      } yield res
  }

  def app(todos: ToDos[IO]): HttpApp[IO] = Router("/todos" -> routes(todos)).orNotFound

  val host: IO[Host] = IO.fromOption(Host.fromString("0.0.0.0"))(new Exception("Can't resolve host"))

  override def run: IO[Unit] =
    host.flatMap { host =>
      ToDos.of[IO].use { todos =>
        EmberServerBuilder
          .default[IO]
          .withHost(host)
          .withHttpApp(app(todos))
          .build
          .use { srv =>
            IO.println(s"HTTP server bound to: ${srv.address}!") *>
              IO.never
          }
      }
    }
}
