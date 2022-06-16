package com.evolutiongaming.bootcamp.tf.practice.effects

import cats.effect.Sync

import scala.io.StdIn

trait Console[F[_]] {
  def readString: F[String]
  def putString(value: String): F[Unit]
}

object Console {

  def apply[F[_]: Sync]: Console[F] =
    new Console[F] {
      def readString: F[String]             = Sync[F].delay(StdIn.readLine())
      def putString(value: String): F[Unit] = Sync[F].delay(println(value))
    }
}
