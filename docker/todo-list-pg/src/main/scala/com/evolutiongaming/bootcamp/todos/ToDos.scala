package com.evolutiongaming.bootcamp.todos

import cats.effect.Async
import cats.effect.kernel.Resource
import cats.effect.std.{Console, UUIDGen}
import cats.syntax.all._
import com.evolutiongaming.bootcamp.todos.ToDos.ToDo
import fs2.io.net.Network
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import natchez.Trace.Implicits._
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import skunk._
import skunk.codec.all._
import skunk.implicits._

import java.util.UUID

trait ToDos[F[_]] {
  def listAll: F[List[ToDo]]

  def create(text: String): F[ToDo]
}

object ToDos {

  private implicit val todo: Codec[ToDo] =
    (uuid ~ varchar(255)).gimap[ToDo]

  private val select: Query[Void, ToDo] =
    sql"SELECT id, text FROM todos".query(todo)

  private val insert: Command[ToDo] =
    sql"INSERT INTO todos(id, text) values($todo)".command

  def of[F[_]: Async: Network: Console: UUIDGen]: Resource[F, ToDos[F]] =
    for {
      conf <- Resource.eval(ConfigSource.default.at("db").loadF[F, DbConfig]())
      _    <- Resource.eval(Console[F].println(s"DB config: $conf"))
      pool <- Session.pooled[F](
        host = conf.host,
        user = conf.username,
        password = conf.password.some,
        database = "todos",
        max = 10,
      )
    } yield new ToDos[F] {
      override def listAll: F[List[ToDo]] =
        pool
          .use { sess =>
            sess.execute(select)
          }
          .onError { case t =>
            Console[F].printStackTrace(t)
          }

      override def create(text: String): F[ToDo] =
        UUIDGen[F].randomUUID
          .flatMap { id =>
            val todo = ToDo(id, text)
            pool.use { sess =>
              sess.prepare(insert).use { pc =>
                pc.execute(todo).as(todo)
              }
            }
          }
          .onError { case t =>
            Console[F].printStackTrace(t)
          }
    }

  final case class ToDo(id: UUID, text: String)

  object ToDo {
    implicit val encoder: Encoder[ToDo] = deriveEncoder
    implicit val decoder: Decoder[ToDo] = deriveDecoder
  }

  private final case class DbConfig(username: String, password: String, host: String)
}
