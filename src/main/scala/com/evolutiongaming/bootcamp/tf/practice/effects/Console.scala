package com.evolutiongaming.bootcamp.tf.practice.effects

import cats.effect.Sync

import scala.io.StdIn

trait Console[F[_]] {
  def readLine: F[String]
  def putLine(value: String): F[Unit]
}

object Console {

  def apply[F[_]: Console]: Console[F] = implicitly

  implicit def syncConsole[F[_]: Sync]: Console[F] =
    new Console[F] {
      def readLine: F[String]             = Sync[F].delay(StdIn.readLine())
      def putLine(value: String): F[Unit] = Sync[F].delay(println(value))
    }
}
