package com.evolutiongaming.bootcamp.tf.shopping

import cats.data.{Kleisli, OptionT}

trait ConsoleInterface[F[_]] {
  def repl: F[Unit]
}

object ConsoleInterface {

  def apply[F[_]](
    router: Kleisli[OptionT[F, *], List[String], String]
  ): ConsoleInterface[F] = ???

}
