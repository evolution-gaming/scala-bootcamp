package com.evolutiongaming.bootcamp.tf.shopping.effects

import cats.effect.Sync

import scala.io.StdIn

trait Console[F[_]] {
  def putStrLn(value: String): F[Unit]
  def readStrLn: F[String]
}

object Console {

  def apply[F[_]: Console]: Console[F] = implicitly

  implicit def console[F[_]: Sync]: Console[F] = new Console[F] {
    override def putStrLn(value: String): F[Unit] = Sync[F].delay(println(value))
    override def readStrLn: F[String]             = Sync[F].delay(StdIn.readLine())
  }

}
