package com.evolutiongaming.bootcamp.tf.practice.effects

import cats.effect.IO

import scala.concurrent.Future

trait ToFuture[F[_]] {
  def apply[A](fa: F[A]): Future[A]
}

object ToFuture {

  def apply[F[_]: ToFuture]: ToFuture[F] = implicitly

  implicit val ioToFuture: ToFuture[IO] = new ToFuture[IO] {
    def apply[A](fa: IO[A]): Future[A] = fa.unsafeToFuture()
  }
}
