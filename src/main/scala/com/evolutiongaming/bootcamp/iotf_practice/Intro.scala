package com.evolutiongaming.bootcamp.iotf_practice

import cats.Monad
import cats.effect.IO

object Intro {
  /*
   * Let's look at two simple functions:
   * Question: what can we say about their operation in the first and second cases?
   */
  def parse1(in: Any): Any            = ???
  def parse2(in: String): Option[Int] = ???

  /*
   * Let's look at a more complicated example:
   * Question: what can we say about their operation in the first and second cases?
   */
  object `1` {
    trait Log {
      def info(message: String): IO[Unit]
    }

    case class Response[A](status: Int, data: A)
    trait Send {
      def toEveryone(id: Long): IO[Response[Boolean]]
    }

    def logAndSend(id: Long, log: Log, send: Send): IO[Boolean] = ???
  }

  object `2` {
    trait Log[F[_]] {
      def info(message: String): F[Unit]
    }

    case class Response[A](status: Int, data: A)
    trait Send[F[_]] {
      def toEveryone(id: Long): F[Response[Boolean]]
    }

    def logAndSend[F[_]: Log: Send: Monad](id: Long): F[Boolean] = ???
  }
}
