package com.evolutiongaming.bootcamp.todos

import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import _root_.io.circe.parser.parse
import _root_.io.circe.syntax._
import _root_.io.circe.{Decoder, Encoder}
import cats.effect.kernel.Sync
import cats.effect.std.{Console, UUIDGen}
import cats.syntax.all._
import com.evolutiongaming.bootcamp.todos.ToDos.ToDo
import fs2._
import fs2.io.file.{Files, Flags, Path}

import java.util.UUID

trait ToDos[F[_]] {
  def listAll: F[List[ToDo]]

  def create(text: String): F[ToDo]
}

object ToDos {

  def of[F[_]: Sync: Files: Console]: ToDos[F] =
    new ToDos[F] {
      val path: Path = Path("todos/todos.txt")

      override def listAll: F[List[ToDo]] =
        Files[F]
          .readAll(path)
          .through(text.utf8.decode)
          .through(text.lines)
          .filter(_.nonEmpty)
          .evalMap { line =>
            Sync[F].fromEither(parse(line).flatMap(_.as[ToDo]))
          }
          .compile
          .toList
          .onError { case t =>
            Console[F].printStackTrace(t)
          }

      override def create(txt: String): F[ToDo] =
        UUIDGen[F].randomUUID
          .map(ToDo(_, txt))
          .flatMap { todo =>
            Stream(todo)
              .map(_.asJson.noSpaces + "\n")
              .through(text.utf8.encode)
              .through(Files[F].writeAll(path, Flags.Append))
              .compile
              .toList
              .as(todo)
              .flatTap { _ =>
                Console[F].println(s"Todo created: $todo")
              }
              .onError { case t =>
                Console[F].printStackTrace(t)
              }
          }
    }

  final case class ToDo(id: UUID, text: String)

  object ToDo {
    implicit val encoder: Encoder[ToDo] = deriveEncoder
    implicit val decoder: Decoder[ToDo] = deriveDecoder
  }
}
