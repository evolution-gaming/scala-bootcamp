package com.evolution.bootcamp.inspect

import cats.effect.Sync

trait Console[F[_]] {

  def putStrLn(text: String): F[Unit]

}
object Console {

  def apply[F[_]](implicit F: Console[F]): Console[F] = F

  def create[F[_]: Sync]: Console[F] = new Console[F] {

    def putStrLn(text: String) = Sync[F].delay {
      println(text)
    }

  }

}
